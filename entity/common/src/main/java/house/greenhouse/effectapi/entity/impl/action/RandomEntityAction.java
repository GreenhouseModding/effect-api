package house.greenhouse.effectapi.entity.impl.action;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.action.RandomAction;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import net.minecraft.util.random.SimpleWeightedRandomList;

public record RandomEntityAction(SimpleWeightedRandomList<EffectAPIAction> actions) implements RandomAction {
    public static final MapCodec<RandomEntityAction> CODEC = RandomAction.codec(
            EffectAPIEntityActionTypes.CODEC, RandomEntityAction::new);

    @Override
    public MapCodec<? extends EffectAPIAction> codec() {
        return CODEC;
    }
}