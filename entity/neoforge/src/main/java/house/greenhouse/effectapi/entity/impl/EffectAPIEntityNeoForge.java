package house.greenhouse.effectapi.entity.impl;

import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityRegistries;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.attachment.EntityEffectsAttachment;
import house.greenhouse.effectapi.entity.api.command.EntityResourceArgument;
import house.greenhouse.effectapi.entity.api.command.EntityResourceValueArgument;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityPredicates;
import house.greenhouse.effectapi.entity.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import house.greenhouse.effectapi.entity.impl.registry.EffectAPIEntityAttachments;
import house.greenhouse.effectapi.entity.platform.EffectAPIEntityPlatformHelperNeoForge;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

@Mod(EffectAPI.MOD_ID + "_entity")
public class EffectAPIEntityNeoForge {
    public EffectAPIEntityNeoForge(IEventBus eventBus) {
        EffectAPIEntity.init(new EffectAPIEntityPlatformHelperNeoForge());
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID + "_entity", bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            register(event, EffectAPIEntityAttachments::registerAll);
            register(event, EffectAPIEntityEffectTypes::registerAll);
            register(event, EffectAPIEntityActionTypes::registerAll);
            register(event, EffectAPIEntityPredicates::registerAll);
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("data_resource"), () -> ArgumentTypeInfos.registerByClass(EntityResourceArgument.class, new EntityResourceArgument.Info()));
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("data_resource_type"), () -> ArgumentTypeInfos.registerByClass(EntityResourceValueArgument.class, new EntityResourceValueArgument.Info()));
        }

        private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0").optional()
                    .playToClient(SyncEntityResourcesAttachmentClientboundPacket.TYPE, SyncEntityResourcesAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(SyncEntityEffectsAttachmentClientboundPacket.TYPE, SyncEntityEffectsAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(ChangeEntityResourceClientboundPacket.TYPE, ChangeEntityResourceClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }

        @SubscribeEvent
        public static void createNewRegistries(NewRegistryEvent event) {
            event.register(EffectAPIEntityRegistries.ACTION_TYPE);
            event.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE);
        }
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID + "_entity", bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity().hasData(EffectAPIEntityAttachments.ENTITY_EFFECTS)) {
                event.getEntity().getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).init(event.getEntity());
                EntityEffectAPI.syncEffects(event.getEntity());
            }
            if (event.getEntity().hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(event.getEntity().getId(), event.getEntity().getData(EffectAPIAttachments.RESOURCES)), event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity().hasData(EffectAPIEntityAttachments.ENTITY_EFFECTS)) {
                event.getEntity().getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).init(event.getEntity());
                EntityEffectAPI.syncEffects(event.getEntity());
            }
            if (event.getEntity().hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(event.getEntity().getId(), event.getEntity().getData(EffectAPIAttachments.RESOURCES)), event.getEntity());
        }

        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getEntity().hasData(EffectAPIEntityAttachments.ENTITY_EFFECTS)) {
                event.getEntity().getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).init(event.getEntity());
                EntityEffectAPI.syncEffects(event.getEntity());
            }
            if (event.getTarget().hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(event.getTarget().getId(), event.getTarget().getData(EffectAPIAttachments.RESOURCES)), event.getTarget());
        }

        @SubscribeEvent
        public static void onPlayerClone(PlayerEvent.Clone event) {
            if (event.getOriginal().hasData(EffectAPIEntityAttachments.ENTITY_EFFECTS)) {
                event.getEntity().setData(EffectAPIEntityAttachments.ENTITY_EFFECTS, event.getOriginal().getData(EffectAPIEntityAttachments.ENTITY_EFFECTS));
                EntityEffectsAttachment attachment = event.getEntity().getData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
                attachment.init(event.getEntity());
                attachment.refresh();
                attachment.sync();
            }
        }
    }
}