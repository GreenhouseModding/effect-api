package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;

public class EffectAPIEntityRegistries {
    public static final Registry<DataComponentType<?>> EFFECT_COMPONENT_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIEntityRegistryKeys.EFFECT_COMPONENT_TYPE);
    public static final Registry<MapCodec<? extends EffectAPIAction>> ACTION_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIEntityRegistryKeys.ACTION_TYPE);
    public static final Registry<MapCodec<? extends Variable<?>>> VARIABLE_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIEntityRegistryKeys.VARIABLE_TYPE);
}
