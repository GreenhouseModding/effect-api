package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.api.EffectAPIDataTypes;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.EffectAPIModifierTypes;
import house.greenhouse.effectapi.api.EffectAPIVariableTypes;
import house.greenhouse.effectapi.api.command.EntityResourceArgument;
import house.greenhouse.effectapi.api.command.EntityResourceValueArgument;
import house.greenhouse.effectapi.api.registry.EffectAPIPredicates;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import house.greenhouse.effectapi.platform.EffectAPIPlatformHelperNeoForge;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

@Mod(EffectAPI.MOD_ID)
public class EffectAPINeoForge {
    public EffectAPINeoForge(IEventBus eventBus) {
        EffectAPI.init(new EffectAPIPlatformHelperNeoForge());
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            register(event, EffectAPIActionTypes::registerAll);
            register(event, EffectAPIAttachments::registerAll);
            register(event, EffectAPIDataTypes::registerAll);
            register(event, EffectAPIEffectTypes::registerAll);
            register(event, EffectAPIModifierTypes::registerAll);
            register(event, EffectAPIPredicates::registerAll);
            register(event, EffectAPIVariableTypes::registerAll);

            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("resource"), () -> ArgumentTypeInfos.registerByClass(EntityResourceArgument.class, new EntityResourceArgument.Info()));
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("resource_value"), () -> ArgumentTypeInfos.registerByClass(EntityResourceValueArgument.class, new EntityResourceValueArgument.Info()));
        }

        private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void createNewRegistries(NewRegistryEvent event) {
            event.register(EffectAPIRegistries.ACTION_TYPE);
            event.register(EffectAPIRegistries.DATA_TYPE);
            event.register(EffectAPIRegistries.EFFECT_TYPE);
            event.register(EffectAPIRegistries.MODIFIER);
            event.register(EffectAPIRegistries.VARIABLE_TYPE);
        }

        @SubscribeEvent
        public static void createNewDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(EffectAPIRegistryKeys.RESOURCE, Resource.DIRECT_CODEC, Resource.DIRECT_CODEC);
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0").optional()
                    .playToClient(SyncEntityResourcesAttachmentClientboundPacket.TYPE, SyncEntityResourcesAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(SyncEntityEffectsAttachmentClientboundPacket.TYPE, SyncEntityEffectsAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(ChangeEntityResourceClientboundPacket.TYPE, ChangeEntityResourceClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            Entity entity = event.getEntity();
            if (entity.hasData(EffectAPIAttachments.RESOURCES)) {
                entity.getData(EffectAPIAttachments.EFFECTS).tick();
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            Entity entity = event.getEntity();
            if (entity.hasData(EffectAPIAttachments.EFFECTS)) {
                EffectsAttachmentImpl attachment = (EffectsAttachmentImpl)entity.getData(EffectAPIAttachments.EFFECTS);
                attachment.init(entity);
                attachment.refresh();
                attachment.sync();
            }
            if (entity.hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(entity.getId(), entity.getData(EffectAPIAttachments.RESOURCES)), entity);
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            Entity entity = event.getEntity();
            if (entity.hasData(EffectAPIAttachments.EFFECTS)) {
                EffectsAttachmentImpl attachment = (EffectsAttachmentImpl)entity.getData(EffectAPIAttachments.EFFECTS);
                attachment.init(entity);
                attachment.refresh();
                attachment.sync();
            }
            if (entity.hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(entity.getId(), entity.getData(EffectAPIAttachments.RESOURCES)), entity);
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            Entity entity = event.getTarget();
            if (entity.hasData(EffectAPIAttachments.EFFECTS)) {
                entity.getData(EffectAPIAttachments.EFFECTS).sync((ServerPlayer) event.getEntity());
            }
            if (event.getTarget().hasData(EffectAPIAttachments.RESOURCES))
                PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new SyncEntityResourcesAttachmentClientboundPacket(entity.getId(), entity.getData(EffectAPIAttachments.RESOURCES)));
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onPlayerClone(PlayerEvent.Clone event) {
            Player original = event.getOriginal();
            Player entity = event.getEntity();
            if (event.getOriginal().hasData(EffectAPIAttachments.EFFECTS)) {
                entity.setData(EffectAPIAttachments.EFFECTS, original.getData(EffectAPIAttachments.EFFECTS));
                EffectsAttachmentImpl attachment = (EffectsAttachmentImpl) entity.getData(EffectAPIAttachments.EFFECTS);
                attachment.init(entity);
                attachment.refresh();
                attachment.sync();
            }
        }
    }
}