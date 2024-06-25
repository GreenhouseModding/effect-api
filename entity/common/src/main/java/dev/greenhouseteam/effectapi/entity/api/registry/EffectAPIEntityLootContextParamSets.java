package dev.greenhouseteam.effectapi.entity.api.registry;

import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EffectAPIEntityLootContextParamSets {
    public static final LootContextParamSet ENTITY = LootContextParamSet.builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .optional(EffectAPILootContextParams.SOURCE)
            .build();
}
