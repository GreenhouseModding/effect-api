package house.greenhouse.effectapi.impl.registry;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.ActionType;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;

public class EffectAPIRegistries {
    public static final Registry<ActionType<?>> ACTION_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.ACTION_TYPE);
    public static final Registry<DataType<?>> DATA_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.DATA_TYPE);
    public static final Registry<EffectType<?>> EFFECT_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.EFFECT_TYPE);
    public static final Registry<MapCodec<? extends Modifier>> MODIFIER = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.MODIFIER);
    public static final Registry<MapCodec<? extends Variable<?>>> VARIABLE_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.VARIABLE_TYPE);
}