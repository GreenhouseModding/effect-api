package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.EffectAPIVariableTypes;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEntityVariableTypes {
    public static final Codec<Variable<?>> CODEC = EffectAPIVariableTypes.codec(EffectAPIEntityRegistries.VARIABLE, EffectAPIEntityLootContextParamSets.ENTITY);

    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIAction>> callback) {

    }
}
