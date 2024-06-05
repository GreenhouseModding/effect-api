package dev.greenhouseteam.effectapi.api.registry;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EffectAPILootContextParamSets {
    public static final LootContextParamSet ENTITY = LootContextParamSet.builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .optional(EffectAPILootContextParams.SOURCE)
            .build();
}
