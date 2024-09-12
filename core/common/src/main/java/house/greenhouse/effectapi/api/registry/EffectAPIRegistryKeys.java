package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<Codec<?>>> RESOURCE_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource_type"));
}