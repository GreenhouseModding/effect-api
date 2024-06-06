package dev.greenhouseteam.effectapi.api.entity;

import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.api.entity.effect.entity.EffectAPIEnchantmentEntityEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEntityInstancedEffectTypes {
    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIInstancedEffect>> callback) {
        callback.register(EffectAPIRegistries.INSTANCED_EFFECT_TYPE, EffectAPIEntity.asResource("enchantment_effect"), EffectAPIEnchantmentEntityEffect.CODEC);
    }
}
