package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.entity.impl.action.AllOfEntityAction;
import house.greenhouse.effectapi.entity.api.effect.EnchantmentEntityEffectAction;
import house.greenhouse.effectapi.entity.impl.action.RandomEntityAction;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEntityActionTypes {
    public static final Codec<EffectAPIAction> CODEC = EffectAPIActionTypes.codec(EffectAPIEntityRegistries.ACTION_TYPE, EffectAPIEntityLootContextParamSets.ENTITY);

    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIAction>> callback) {
        callback.register(EffectAPIEntityRegistries.ACTION_TYPE, EffectAPI.asResource("all_of"), AllOfEntityAction.CODEC);
        callback.register(EffectAPIEntityRegistries.ACTION_TYPE, EffectAPI.asResource("enchantment_entity_effect"), EnchantmentEntityEffectAction.CODEC);
        callback.register(EffectAPIEntityRegistries.ACTION_TYPE, EffectAPI.asResource("random"), RandomEntityAction.CODEC);
    }
}
