package house.greenhouse.effectapi.api.registry;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;

public class EffectAPIRegistries {
    public static final Registry<Codec<?>> VARIABLE_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIRegistryKeys.VARIABLE_TYPE);
}
