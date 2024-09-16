package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<DataType<?>>> DATA_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("data_type"));
    public static final ResourceKey<Registry<Resource<?>>> RESOURCE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource"));
    public static final ResourceKey<Registry<MapCodec<? extends Variable<?>>>> VARIABLE = ResourceKey.createRegistryKey(EffectAPI.asResource("variable"));
}