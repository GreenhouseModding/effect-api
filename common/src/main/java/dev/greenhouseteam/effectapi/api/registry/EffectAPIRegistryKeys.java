package dev.greenhouseteam.effectapi.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIEntityEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<DataComponentType<?>>> EFFECT = ResourceKey.createRegistryKey(EffectAPI.asResource("effect"));
    public static final ResourceKey<Registry<MapCodec<? extends EffectAPIEntityEffect>>> ENTITY_EFFECT = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_effect"));
    public static final ResourceKey<Registry<Codec<?>>> RESOURCE_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource_type"));
}