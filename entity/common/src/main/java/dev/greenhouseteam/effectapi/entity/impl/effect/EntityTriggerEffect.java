package dev.greenhouseteam.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.TriggerEffect;
import dev.greenhouseteam.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.entity.api.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Optional;

public record EntityTriggerEffect(Optional<EffectAPIInstancedEffect> onAddedEffect, Optional<EffectAPIInstancedEffect> onRemovedEffect) implements TriggerEffect {
    public static final Codec<EntityTriggerEffect> CODEC = TriggerEffect.createCodec(EffectAPIEntityLootContextParamSets.ENTITY, EntityTriggerEffect::new);

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TRIGGER;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
