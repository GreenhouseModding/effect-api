package dev.greenhouseteam.effectapi.api.registry;

import dev.greenhouseteam.effectapi.impl.EffectAPIEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class EffectAPILootContextParams {
    public static final LootContextParam<ResourceLocation> SOURCE = new LootContextParam<>(EffectAPIEntity.asResource("source"));
}
