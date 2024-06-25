package dev.greenhouseteam.effectapi.entity.api;

import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.entity.api.effect.EntityAttributeEffect;
import dev.greenhouseteam.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import dev.greenhouseteam.effectapi.entity.impl.effect.EntityResourceEffect;
import dev.greenhouseteam.effectapi.entity.impl.effect.EntityTickEffect;
import dev.greenhouseteam.effectapi.entity.impl.effect.EntityTriggerEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
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
