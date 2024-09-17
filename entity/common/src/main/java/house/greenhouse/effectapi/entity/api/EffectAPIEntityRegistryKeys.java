package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;

public class EffectAPIEntityRegistryKeys {
    public static final ResourceKey<Registry<MapCodec<? extends EffectAPIAction>>> ACTION_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_action_type"));
    public static final ResourceKey<Registry<EffectType<?, Entity>>> EFFECT_TYPE = ResourceKey.createRegistryKey(EffectAPI.asResource("entity_effect_type"));
}
