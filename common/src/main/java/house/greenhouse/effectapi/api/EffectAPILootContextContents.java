package house.greenhouse.effectapi.api;

import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EffectAPILootContextContents {
    public static final LootContextParam<ResourceLocation> SOURCE = new LootContextParam<>(EffectAPI.asResource("source"));

    public static final LootContextParamSet ENTITY = LootContextParamSet.builder()
            .required(LootContextParams.THIS_ENTITY)
            .required(LootContextParams.ORIGIN)
            .optional(SOURCE)
            .build();
}
