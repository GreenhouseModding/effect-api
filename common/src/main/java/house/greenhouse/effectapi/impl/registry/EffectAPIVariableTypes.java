package house.greenhouse.effectapi.impl.registry;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.variable.ModifierVariable;
import house.greenhouse.effectapi.api.variable.NumberProviderVariable;
import house.greenhouse.effectapi.api.variable.Variable;

import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIVariableTypes {

    public static void registerAll(RegistrationCallback<MapCodec<? extends Variable<?>>> callback) {
        callback.register(EffectAPIRegistries.VARIABLE_TYPE, EffectAPI.asResource("number_provider"), NumberProviderVariable.CODEC);
        callback.register(EffectAPIRegistries.VARIABLE_TYPE, EffectAPI.asResource("modifier"), ModifierVariable.CODEC);
    }
}
