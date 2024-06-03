
package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIEffects;
import dev.greenhouseteam.effectapi.api.EffectAPIEntityEffects;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIEntityEffect;
import dev.greenhouseteam.effectapi.api.params.EffectAPILootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record EffectAPITickEffect<T extends EffectAPIEntityEffect>(T effect, LootContextParamSet set) implements EffectAPIEffect {
    public static Codec<EffectAPITickEffect<?>> codec(LootContextParamSet set) {
        return EffectAPIEntityEffect.CODEC.xmap(effect -> new EffectAPITickEffect<>(effect, set), EffectAPITickEffect::effect);
    }

    public void tick(LootContext lootContext) {
        effect.apply(lootContext);
    }

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEffects.TICK;
    }

    @Override
    public LootContextParamSet paramSet() {
        return set;
    }
}
