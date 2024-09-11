package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<DataComponentType<?>>> EFFECT_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("effect_type"));
    public static final ResourceKey<Registry<MapCodec<? extends EffectAPIInstancedEffect>>> ENTITY_EFFECT_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("instanced_effect_type"));
    public static final ResourceKey<Registry<Codec<?>>> RESOURCE_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource_type"));
}