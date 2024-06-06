package dev.greenhouseteam.effectapi.api;

import dev.greenhouseteam.effectapi.api.effect.*;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.impl.EffectAPIEntity;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEffectTypes {

    public static final DataComponentType<List<EffectAPIConditionalEffect<AttributeEffect>>> ATTRIBUTE = DataComponentType.<List<EffectAPIConditionalEffect<AttributeEffect>>>builder()           .persistent(EffectAPIConditionalEffect.codec(AttributeEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>> ENTITY_TICK = DataComponentType.<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EntityResourceEffect<?>>> RESOURCE = DataComponentType.<List<EntityResourceEffect<?>>>builder()
            .persistent(EntityResourceEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTriggerEffect>>> TRIGGER = DataComponentType.<List<EffectAPIConditionalEffect<EntityTriggerEffect>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("attribute"), ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("entity_tick"), ENTITY_TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPIEntity.asResource("trigger"), TRIGGER);
    }
}
