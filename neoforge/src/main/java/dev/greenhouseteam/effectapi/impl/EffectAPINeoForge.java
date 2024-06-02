package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIEffects;
import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.command.DataResourceArgument;
import dev.greenhouseteam.effectapi.api.command.DataResourceValueArgument;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.api.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import dev.greenhouseteam.effectapi.platform.EffectAPIPlatformHelperNeoForge;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("data_resource"), () -> ArgumentTypeInfos.registerByClass(DataResourceArgument.class, SingletonArgumentInfo.contextFree(DataResourceArgument::resource)));
            event.register(Registries.COMMAND_ARGUMENT_TYPE, EffectAPI.asResource("data_resource_type"), () -> ArgumentTypeInfos.registerByClass(DataResourceValueArgument.class, new DataResourceValueArgument.Info()));
            register(event, EffectAPIAttachments::registerAll);
            register(event, EffectAPIEffects::registerAll);
            register(event, EffectAPIResourceTypes::registerAll);
        }

        private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0")
                    .playToClient(ChangeResourceClientboundPacket.TYPE, ChangeResourceClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }

        @SubscribeEvent
        public static void createNewRegistries(NewRegistryEvent event) {
            event.register(EffectAPIRegistries.EFFECT);
            event.register(EffectAPIRegistries.RESOURCE_TYPE);
        }
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getTarget().hasData(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncResourcesAttachmentClientboundPacket(event.getTarget().getId(), event.getTarget().getData(EffectAPIAttachments.RESOURCES)), event.getTarget());
        }

        @SubscribeEvent
        public static void onServerStart(ServerStartedEvent event) {
            ResourceEffect.EffectCodec.setRegistryPhase(false);
            ResourceEffect.EffectCodec.clearLoadedIds();
        }

        @SubscribeEvent
        public static void onServerStop(ServerStartedEvent event) {
            ResourceEffect.EffectCodec.setRegistryPhase(true);
        }
    }
}