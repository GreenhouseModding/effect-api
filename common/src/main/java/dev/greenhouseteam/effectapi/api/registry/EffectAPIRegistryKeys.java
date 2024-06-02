package dev.greenhouseteam.effectapi.api.registry;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<DataComponentType<?>>> EFFECT = ResourceKey.createRegistryKey(EffectAPI.asResource("effect"));
    public static final ResourceKey<Registry<Codec<?>>> RESOURCE_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource_type"));
}