
package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIEntityEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public record EffectAPITickEffect<T extends EffectAPIEntityEffect>(T effect, LootContextParamSet set) implements EffectAPIEffect {
    public static Codec<EffectAPITickEffect<?>> codec(LootContextParamSet set) {
        return EffectAPIEntityEffect.CODEC.xmap(effect -> new EffectAPITickEffect<>(effect, set), EffectAPITickEffect::effect);
    }

    public static Codec<EffectAPIConditionalEffect<EffectAPITickEffect<?>>> conditionalCodec(LootContextParamSet set) {
        return EffectAPIConditionalEffect.codec(EffectAPIEntityEffect.CODEC.xmap(effect -> new EffectAPITickEffect<>(effect, set), EffectAPITickEffect::effect), set);
    }

    public void tick(LootContext lootContext) {
        effect.apply(lootContext);
    }

    @Override
    public LootContextParamSet paramSet() {
        return set;
    }
}
