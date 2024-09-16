package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;

public class EffectAPIRegistries {
    public static final Registry<DataType<?>> DATA_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.DATA_TYPE);
    public static final Registry<MapCodec<? extends Variable<?>>> VARIABLE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.VARIABLE);
}
