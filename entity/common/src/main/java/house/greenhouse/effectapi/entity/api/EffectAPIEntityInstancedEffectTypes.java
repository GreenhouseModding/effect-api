package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.entity.api.effect.EffectAPIEnchantmentEntityEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEntityInstancedEffectTypes {
    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIInstancedEffect>> callback) {
        callback.register(EffectAPIRegistries.INSTANCED_EFFECT_TYPE, EffectAPI.asResource("enchantment_effect"), EffectAPIEnchantmentEntityEffect.CODEC);
    }
}
