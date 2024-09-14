package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<Codec<?>>> VARIABLE_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("variable_type"));
    public static final ResourceKey<Registry<Resource<?>>> RESOURCE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource"));
}