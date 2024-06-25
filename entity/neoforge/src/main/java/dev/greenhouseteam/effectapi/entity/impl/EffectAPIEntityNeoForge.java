package dev.greenhouseteam.effectapi.entity.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.entity.api.command.EntityResourceArgument;
import dev.greenhouseteam.effectapi.entity.api.command.EntityResourceValueArgument;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.SyncEffectsAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.entity.platform.EffectAPIEntityPlatformHelperNeoForge;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
            register(event, EffectAPIResourceTypes::registerAll);
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("data_resource"), () -> ArgumentTypeInfos.registerByClass(EntityResourceArgument.class, new EntityResourceArgument.Info()));
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("data_resource_type"), () -> ArgumentTypeInfos.registerByClass(EntityResourceValueArgument.class, new EntityResourceValueArgument.Info()));
        }

        private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0")
                    .playToClient(SyncResourcesAttachmentClientboundPacket.TYPE, SyncResourcesAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(SyncEffectsAttachmentClientboundPacket.TYPE, SyncEffectsAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle())
                    .playToClient(ChangeResourceClientboundPacket.TYPE, ChangeResourceClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID + "_entity", bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getTarget().hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncResourcesAttachmentClientboundPacket(event.getTarget().getId(), event.getTarget().getData(EffectAPIAttachments.RESOURCES)), event.getTarget());
        }
    }
}