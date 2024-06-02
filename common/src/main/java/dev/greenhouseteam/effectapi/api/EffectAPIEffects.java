package dev.greenhouseteam.effectapi.api;

import dev.greenhouseteam.effectapi.api.effect.ConditionedEffect;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEffects {
    public static final DataComponentType<List<ConditionedEffect>> CONDITIONED = DataComponentType.<List<ConditionedEffect>>builder()
            .persistent(ConditionedEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<ResourceEffect<?>>> RESOURCE = DataComponentType.<List<ResourceEffect<?>>>builder()
            .persistent(ResourceEffect.CODEC.listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT, EffectAPI.asResource("conditioned"), CONDITIONED);
        callback.register(EffectAPIRegistries.EFFECT, EffectAPI.asResource("resource"), RESOURCE);
    }
}
