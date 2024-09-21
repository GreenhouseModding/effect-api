package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.function.Function;


public record ActionType<A extends EffectAPIAction>(Function<LootContextParamSet, MapCodec<A>> codecFunction) {
    public MapCodec<A> codec(LootContextParamSet paramSet) {
        return codecFunction.apply(paramSet);
    }
}
