package dev.greenhouseteam.effectapi.api.entity;

import dev.greenhouseteam.effectapi.api.entity.effect.*;
import dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEntityEffectTypes {

    public static final DataComponentType<List<dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect<AttributeEffect>>> ATTRIBUTE = DataComponentType.<List<dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect<AttributeEffect>>>builder()           .persistent(dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect.codec(AttributeEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect<EntityTickEffect<?>>>> ENTITY_TICK = DataComponentType.<List<dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect<EntityTickEffect<?>>>>builder()
            .persistent(dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EntityResourceEffect<?>>> RESOURCE = DataComponentType.<List<EntityResourceEffect<?>>>builder()
            .persistent(EntityResourceEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect<EntityTriggerEffect>>> TRIGGER = DataComponentType.<List<dev.greenhouseteam.effectapi.api.entity.effect.EffectAPIConditionalEffect<EntityTriggerEffect>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("attribute"), ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("entity_tick"), ENTITY_TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("trigger"), TRIGGER);
    }
}
