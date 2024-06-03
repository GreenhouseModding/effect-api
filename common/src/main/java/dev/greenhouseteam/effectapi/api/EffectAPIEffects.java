package dev.greenhouseteam.effectapi.api;

import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPITickEffect;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.api.params.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEffects {
    public static final DataComponentType<List<ResourceEffect<?>>> RESOURCE = DataComponentType.<List<ResourceEffect<?>>>builder()
            .persistent(ResourceEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EffectAPITickEffect<?>>>> TICK = DataComponentType.<List<EffectAPIConditionalEffect<EffectAPITickEffect<?>>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EffectAPITickEffect.codec(EffectAPILootContextParamSets.ENTITY), EffectAPILootContextParamSets.ENTITY).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT, EffectAPI.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT, EffectAPI.asResource("tick"), TICK);
    }
}
