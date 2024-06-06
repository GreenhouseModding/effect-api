package dev.greenhouseteam.effectapi.api.entity.registry;

import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class EffectAPILootContextParams {
    public static final LootContextParam<ResourceLocation> SOURCE = new LootContextParam<>(EffectAPIEntity.asResource("source"));
}
