package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import dev.greenhouseteam.effectapi.platform.EffectAPIBasePlatformHelperNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;

@Mod(EffectAPI.MOD_ID + "_base")
public class EffectAPIBaseNeoForge {
    public EffectAPIBaseNeoForge(IEventBus eventBus) {
        EffectAPI.init(new EffectAPIBasePlatformHelperNeoForge());
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID + "_base", bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            register(event, EffectAPIResourceTypes::registerAll);
        }

        private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void createNewRegistries(NewRegistryEvent event) {
            event.register(EffectAPIRegistries.EFFECT_TYPE);
            event.register(EffectAPIRegistries.INSTANCED_EFFECT_TYPE);
            event.register(EffectAPIRegistries.RESOURCE_TYPE);
        }
    }

    @EventBusSubscriber(modid = EffectAPI.MOD_ID + "_base", bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onServerStart(ServerStartedEvent event) {
            ResourceEffect.ResourceEffectCodec.setRegistryPhase(false);
            ResourceEffect.ResourceEffectCodec.clearLoadedIds();
        }

        @SubscribeEvent
        public static void onServerStop(ServerStartedEvent event) {
            ResourceEffect.ResourceEffectCodec.setRegistryPhase(true);
        }
    }
}