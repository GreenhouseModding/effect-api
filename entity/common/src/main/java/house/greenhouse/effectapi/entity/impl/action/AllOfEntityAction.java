package house.greenhouse.effectapi.entity.impl.action;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.AllOfAction;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;

import java.util.List;

public record AllOfEntityAction(List<EffectAPIAction> actions) implements AllOfAction {
    public static final MapCodec<AllOfEntityAction> CODEC = AllOfAction.createCodec(
            EffectAPIEntityActionTypes.CODEC, AllOfEntityAction::new);

    @Override
    public MapCodec<? extends EffectAPIAction> codec() {
        return CODEC;
    }
}