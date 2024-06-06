package dev.greenhouseteam.effectapi.api.effect.entity;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Collection;

public interface EffectAPIInstancedEffect {
    void apply(LootContext context);

    Collection<LootContextParam<?>> requiredParams();

    MapCodec<? extends EffectAPIInstancedEffect> codec();
}
