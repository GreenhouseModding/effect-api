package house.greenhouse.effectapi.entity.api;

import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.entity.api.effect.EntityAttributeEffect;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityTickEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityTriggerEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEntityEffectTypes {

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
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("entity_attribute"), ENTITY_ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("entity_tick"), ENTITY_TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("entity_resource"), ENTITY_RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("entity_trigger"), ENTITY_TRIGGER);
    }
}
