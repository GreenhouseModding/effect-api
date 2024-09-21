package house.greenhouse.effectapi.api;

import house.greenhouse.effectapi.api.action.ActionType;
import house.greenhouse.effectapi.api.action.AllOfAction;
import house.greenhouse.effectapi.api.action.EnchantmentEntityEffectAction;
import house.greenhouse.effectapi.api.action.RandomAction;
import house.greenhouse.effectapi.impl.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIActionTypes {
    public static void registerAll(RegistrationCallback<ActionType<?>> callback) {
        callback.register(EffectAPIRegistries.ACTION_TYPE, EffectAPI.asResource("all_of"), AllOfAction.TYPE);
        callback.register(EffectAPIRegistries.ACTION_TYPE, EffectAPI.asResource("enchantment_entity_effect"), EnchantmentEntityEffectAction.TYPE);
        callback.register(EffectAPIRegistries.ACTION_TYPE, EffectAPI.asResource("random"), RandomAction.TYPE);
    }
}
