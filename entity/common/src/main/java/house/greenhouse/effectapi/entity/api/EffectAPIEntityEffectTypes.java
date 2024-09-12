package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.entity.api.effect.EntityAttributeEffect;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityTickEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityTriggerEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.List;

public class EffectAPIEntityEffectTypes {
    public static Codec<DataComponentMap> codec(LootContextParamSet params) {
        return EffectAPIEffectTypes.codec(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, params);
    }

    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityAttributeEffect>>> ENTITY_ATTRIBUTE = DataComponentType.<List<EffectAPIConditionalEffect<EntityAttributeEffect>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityAttributeEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>> ENTITY_TICK = DataComponentType.<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EntityResourceEffect<?>>> ENTITY_RESOURCE = DataComponentType.<List<EntityResourceEffect<?>>>builder()
            .persistent(EntityResourceEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTriggerEffect>>> ENTITY_TRIGGER = DataComponentType.<List<EffectAPIConditionalEffect<EntityTriggerEffect>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("entity_attribute"), ENTITY_ATTRIBUTE);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("entity_tick"), ENTITY_TICK);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("entity_resource"), ENTITY_RESOURCE);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("entity_trigger"), ENTITY_TRIGGER);
    }
}
