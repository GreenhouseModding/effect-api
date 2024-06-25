package dev.greenhouseteam.effectapi.entity.api;

import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.entity.api.effect.AttributeEffect;
import dev.greenhouseteam.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import dev.greenhouseteam.effectapi.entity.impl.EffectAPIEntity;
import dev.greenhouseteam.effectapi.entity.impl.effect.EntityResourceEffect;
import dev.greenhouseteam.effectapi.entity.impl.effect.EntityTickEffect;
import dev.greenhouseteam.effectapi.entity.impl.effect.EntityTriggerEffect;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEntityEffectTypes {

    public static final DataComponentType<List<EffectAPIConditionalEffect<AttributeEffect>>> ATTRIBUTE = DataComponentType.<List<EffectAPIConditionalEffect<AttributeEffect>>>builder()           .persistent(EffectAPIConditionalEffect.codec(AttributeEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>> ENTITY_TICK = DataComponentType.<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EntityResourceEffect<?>>> RESOURCE = DataComponentType.<List<EntityResourceEffect<?>>>builder()
            .persistent(EntityResourceEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTriggerEffect>>> TRIGGER = DataComponentType.<List<EffectAPIConditionalEffect<EntityTriggerEffect>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("attribute"), ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("entity_tick"), ENTITY_TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("trigger"), TRIGGER);
    }
}
