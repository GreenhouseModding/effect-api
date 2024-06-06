package dev.greenhouseteam.effectapi.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;

public class EffectAPIRegistries {
    public static final Registry<DataComponentType<?>> EFFECT_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.EFFECT_TYPE);
    public static final Registry<MapCodec<? extends EffectAPIInstancedEffect>> INSTANCED_EFFECT_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.ENTITY_EFFECT_TYPE);
    public static final Registry<Codec<?>> RESOURCE_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.RESOURCE_TYPE);
}
