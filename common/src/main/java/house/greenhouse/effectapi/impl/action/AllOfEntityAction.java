package house.greenhouse.effectapi.impl.action;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.api.action.AllOfAction;
import house.greenhouse.effectapi.api.action.EffectAPIAction;

import java.util.List;

public record AllOfEntityAction(List<EffectAPIAction> actions) implements AllOfAction {
    public static final MapCodec<AllOfEntityAction> CODEC = AllOfAction.codec(
            EffectAPIActionTypes.CODEC, AllOfEntityAction::new);

    @Override
    public MapCodec<? extends EffectAPIAction> codec() {
        return CODEC;
    }
}