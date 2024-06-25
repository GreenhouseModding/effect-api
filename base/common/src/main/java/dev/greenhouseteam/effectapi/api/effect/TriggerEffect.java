package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Optional;
import java.util.function.BiFunction;

public interface TriggerEffect extends EffectAPIEffect {
    static <T extends TriggerEffect> Codec<T> createCodec(LootContextParamSet paramSet, BiFunction<Optional<EffectAPIInstancedEffect>, Optional<EffectAPIInstancedEffect>, T> constructor) {
        return RecordCodecBuilder.create(inst -> inst.group(
                EffectAPIInstancedEffectTypes.codec(paramSet).optionalFieldOf("on_added").forGetter(TriggerEffect::onAddedEffect),
                EffectAPIInstancedEffectTypes.codec(paramSet).optionalFieldOf("on_removed").forGetter(TriggerEffect::onRemovedEffect)
        ).apply(inst, constructor));
    }

    @Override
    default void onAdded(LootContext context) {
        onAddedEffect().ifPresent(effect -> effect.apply(context));
    }

    @Override
    default void onRemoved(LootContext context) {
        onRemovedEffect().ifPresent(effect -> effect.apply(context));
    }

    Optional<EffectAPIInstancedEffect> onAddedEffect();

    Optional<EffectAPIInstancedEffect> onRemovedEffect();
}
