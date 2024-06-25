
package dev.greenhouseteam.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.TickEffect;
import dev.greenhouseteam.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.entity.api.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public record EntityTickEffect<T extends EffectAPIInstancedEffect>(T effect) implements TickEffect<T> {
    public static final Codec<EntityTickEffect<?>> CODEC = EffectAPIInstancedEffectTypes.codec(EffectAPIEntityLootContextParamSets.ENTITY).xmap(EntityTickEffect::new, EntityTickEffect::effect);

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TICK;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
