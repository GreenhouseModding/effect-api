package dev.greenhouseteam.effectapi.api.entity.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.api.entity.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Optional;

public record EntityTriggerEffect(Optional<EffectAPIInstancedEffect> onActivationEffect, Optional<EffectAPIInstancedEffect> onDeactivationEffect) implements EffectAPIEffect {
    public static final Codec<EntityTriggerEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EffectAPIInstancedEffectTypes.codec(dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets.ENTITY).optionalFieldOf("on_activation").forGetter(EntityTriggerEffect::onActivationEffect),
            EffectAPIInstancedEffectTypes.codec(dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets.ENTITY).optionalFieldOf("on_deactivation").forGetter(EntityTriggerEffect::onDeactivationEffect)
    ).apply(inst, EntityTriggerEffect::new));

    @Override
    public void onAdded(LootContext context) {
        onActivationEffect.ifPresent(effect -> effect.apply(context));
    }

    @Override
    public void onRemoved(LootContext context) {
        onDeactivationEffect.ifPresent(effect -> effect.apply(context));
    }

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.TRIGGER;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPILootContextParamSets.ENTITY;
    }
}
