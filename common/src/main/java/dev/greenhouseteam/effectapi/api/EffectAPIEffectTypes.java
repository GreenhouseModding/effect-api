package dev.greenhouseteam.effectapi.api;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.*;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIEffectTypes {
    private static final Codec<DataComponentType<?>> COMPONENT_CODEC = Codec.lazyInitialized(EffectAPIRegistries.EFFECT_TYPE::byNameCodec);
    public static final Codec<DataComponentMap> CODEC = DataComponentMap.makeCodec(COMPONENT_CODEC);

    public static final DataComponentType<List<EffectAPIConditionalEffect<AttributeEffect>>> ATTRIBUTE = DataComponentType.<List<EffectAPIConditionalEffect<AttributeEffect>>>builder()           .persistent(EffectAPIConditionalEffect.codec(AttributeEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>> ENTITY_TICK = DataComponentType.<List<EffectAPIConditionalEffect<EntityTickEffect<?>>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();
    public static final DataComponentType<List<ResourceEffect<?>>> RESOURCE = DataComponentType.<List<ResourceEffect<?>>>builder()
            .persistent(ResourceEffect.CODEC.listOf())
            .build();
    public static final DataComponentType<List<EffectAPIConditionalEffect<TriggerEffect>>> TRIGGER = DataComponentType.<List<EffectAPIConditionalEffect<TriggerEffect>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(TriggerEffect.CODEC, EffectAPILootContextParamSets.ENTITY).listOf())
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("attribute"), ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("entity_tick"), ENTITY_TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("trigger"), TRIGGER);
    }
}
