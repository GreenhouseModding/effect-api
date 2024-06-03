package dev.greenhouseteam.effectapi.api;

import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIEnchantmentEntityEffect;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIEntityEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEntityEffects {

    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIEntityEffect>> callback) {
        callback.register(EffectAPIRegistries.ENTITY_EFFECT, EffectAPI.asResource("enchantment_effect"), EffectAPIEnchantmentEntityEffect.CODEC);
    }
}
