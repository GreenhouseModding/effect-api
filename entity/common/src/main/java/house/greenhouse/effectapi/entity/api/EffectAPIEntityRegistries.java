package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;

public class EffectAPIEntityRegistries {
    public static final Registry<MapCodec<? extends EffectAPIAction>> ACTION_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIEntityRegistryKeys.ACTION_TYPE);
    public static final Registry<EffectType<?, Entity>> EFFECT_TYPE = EffectAPI.getHelper().createRegistry(EffectAPIEntityRegistryKeys.EFFECT_TYPE);
}
