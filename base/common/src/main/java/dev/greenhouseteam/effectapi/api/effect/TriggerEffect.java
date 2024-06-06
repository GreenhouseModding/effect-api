package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Optional;
import java.util.function.BiFunction;

public interface TriggerEffect extends EffectAPIEffect {
    static <T extends TriggerEffect> Codec<T> createCodec(LootContextParamSet paramSet, BiFunction<Optional<EffectAPIInstancedEffect>, Optional<EffectAPIInstancedEffect>, T> constructor) {
        return RecordCodecBuilder.create(inst -> inst.group(
                EffectAPIInstancedEffectTypes.codec(EffectAPILootContextParamSets.ENTITY).optionalFieldOf("on_activation").forGetter(TriggerEffect::onActivationEffect),
                EffectAPIInstancedEffectTypes.codec(EffectAPILootContextParamSets.ENTITY).optionalFieldOf("on_deactivation").forGetter(TriggerEffect::onDeactivationEffect)
        ).apply(inst, (t1, t2) -> constructor.apply(t1, t2)));
    }

    @Override
    default void onAdded(LootContext context) {
        onActivationEffect().ifPresent(effect -> effect.apply(context));
    }

    @Override
    default void onRemoved(LootContext context) {
        onDeactivationEffect().ifPresent(effect -> effect.apply(context));
    }

    Optional<EffectAPIInstancedEffect> onActivationEffect();

    Optional<EffectAPIInstancedEffect> onDeactivationEffect();
}
