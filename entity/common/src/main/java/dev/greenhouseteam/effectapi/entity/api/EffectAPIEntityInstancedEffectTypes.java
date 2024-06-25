package dev.greenhouseteam.effectapi.entity.api;

import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.entity.api.effect.EffectAPIEnchantmentEntityEffect;
import dev.greenhouseteam.effectapi.entity.impl.EffectAPIEntity;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEntityInstancedEffectTypes {
    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIInstancedEffect>> callback) {
        callback.register(EffectAPIRegistries.INSTANCED_EFFECT_TYPE, EffectAPIEntity.asResource("enchantment_effect"), EffectAPIEnchantmentEntityEffect.CODEC);
    }
}
