package dev.greenhouseteam.effectapi.api.effect.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.function.Function;

public interface EffectAPIEntityEffect {
    Codec<EffectAPIEntityEffect> CODEC = EffectAPIRegistries.ENTITY_EFFECT_TYPE.byNameCodec().dispatch(EffectAPIEntityEffect::codec, Function.identity());

    void apply(LootContext lootContext);

    MapCodec<? extends EffectAPIEntityEffect> codec();
}
