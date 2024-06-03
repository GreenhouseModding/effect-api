package dev.greenhouseteam.test;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.RegistryFixedCodec;

public record Power(DataComponentMap effects) {
    public static final Codec<Power> DIRECT_CODEC = EffectAPIEffect.CODEC.xmap(Power::new, Power::effects);
    public static final Codec<Holder<Power>> CODEC = RegistryFixedCodec.create(EffectAPITest.POWER);
}
