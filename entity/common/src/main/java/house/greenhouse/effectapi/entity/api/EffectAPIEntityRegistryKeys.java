package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

public class EffectAPIEntityRegistryKeys {
    public static final ResourceKey<Registry<DataComponentType<?>>> EFFECT_COMPONENT_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_effect_component_type"));
    public static final ResourceKey<Registry<MapCodec<? extends EffectAPIAction>>> ACTION_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_action_type"));
}
