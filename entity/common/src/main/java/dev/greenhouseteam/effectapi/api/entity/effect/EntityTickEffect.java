
package dev.greenhouseteam.effectapi.api.entity.effect;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.TickEffect;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.api.entity.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public record EntityTickEffect<T extends EffectAPIInstancedEffect>(T effect) implements TickEffect<T> {
    public static final Codec<EntityTickEffect<?>> CODEC = EffectAPIInstancedEffectTypes.codec(EffectAPILootContextParamSets.ENTITY).xmap(EntityTickEffect::new, EntityTickEffect::effect);

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TICK;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPILootContextParamSets.ENTITY;
    }
}
