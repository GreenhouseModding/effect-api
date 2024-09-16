package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<DataType<?>>> DATA_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("data_type"));
    public static final ResourceKey<Registry<MapCodec<? extends Modifier>>> MODIFIER = ResourceKey.createRegistryKey(EffectAPI.asResource("modifier"));
    public static final ResourceKey<Registry<Resource<?>>> RESOURCE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource"));
}