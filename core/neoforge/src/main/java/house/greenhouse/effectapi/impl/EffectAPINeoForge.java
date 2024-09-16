package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.api.EffectAPIDataTypes;
import house.greenhouse.effectapi.api.EffectAPIModifierTypes;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import house.greenhouse.effectapi.platform.EffectAPIPlatformHelperNeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
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
            register(event, EffectAPIAttachments::registerAll);
            register(event, EffectAPIDataTypes::registerAll);
            register(event, EffectAPIModifierTypes::registerAll);
        }

        private static <T> void register(RegisterEvent event, Consumer<RegistrationCallback<T>> consumer) {
            consumer.accept((registry, id, value) ->
                    event.register(registry.key(), id, () -> value));
        }

        @SubscribeEvent
        public static void createNewRegistries(NewRegistryEvent event) {
            event.register(EffectAPIRegistries.DATA_TYPE);
            event.register(EffectAPIRegistries.MODIFIER);
        }
        @SubscribeEvent
        public static void createNewDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(EffectAPIRegistryKeys.RESOURCE, Resource.DIRECT_CODEC, Resource.DIRECT_CODEC);
        }
    }
}