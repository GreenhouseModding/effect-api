package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class EffectAPIRegistryKeys {
    public static final ResourceKey<Registry<MapCodec<? extends EffectAPIAction>>> ACTION_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_action_type"));
    public static final ResourceKey<Registry<DataType<?>>> DATA_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("data_type"));
    public static final ResourceKey<Registry<EffectType<?>>> EFFECT_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_effect_type"));
    public static final ResourceKey<Registry<MapCodec<? extends Modifier>>> MODIFIER = ResourceKey.createRegistryKey(EffectAPI.asResource("modifier"));
    public static final ResourceKey<Registry<Resource<?>>> RESOURCE = ResourceKey.createRegistryKey(EffectAPI.asResource("resource"));
    public static final ResourceKey<Registry<MapCodec<? extends Variable<?>>>> VARIABLE_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("variable_type"));
}