package house.greenhouse.effectapi.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import house.greenhouse.effectapi.api.effect.DummyEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.effect.EntityAttributeEffect;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParamSets;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.EffectAPIEffectTypeInternals;
import house.greenhouse.effectapi.impl.effect.EffectHolderImpl;
import house.greenhouse.effectapi.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.impl.effect.EntityTickEffect;
import house.greenhouse.effectapi.impl.effect.EntityTriggerEffect;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Map;
import java.util.stream.Collectors;

public class EffectAPIEffectTypes {

    public static Codec<DataComponentMap> codec(Registry<EffectType<?>> typeRegistry) {
        return Codec.lazyInitialized(() -> Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> type.codec().listOf()).flatXmap(dataComponentMap -> EffectAPIEffectTypeInternals.encodeComponents(dataComponentMap), map -> (DataResult) EffectAPIEffectTypeInternals.decodeComponents(map)));
    }

    public static Codec<DataComponentMap> variableAllowedCodec(Registry<EffectType<?>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodec(type.codec(), EffectAPIVariableTypes.validatedCodec(paramSet)).listOf()).flatXmap(dataComponentMap -> {
            var map = dataComponentMap.entrySet().stream().map(entry ->
                    Pair.of(entry.getKey(), entry.getValue().stream().map(variableHolder -> new EffectHolderImpl(entry.getKey(), variableHolder)).toList())
            ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            return EffectAPIEffectTypeInternals.encodeComponents((Map<EffectType<?>, ?>)(Map<?, ?>) map);
        }, map -> (DataResult)EffectAPIEffectTypeInternals.decodeComponents(map));
    }

    public static Codec<DataComponentMap> variableAllowedNetworkCodec(Registry<EffectType<?>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodecForNetwork(type.codec(), EffectAPIVariableTypes.validatedCodec(paramSet)).listOf()).flatXmap(dataComponentMap -> EffectAPIEffectTypeInternals.encodeComponents(dataComponentMap), map -> (DataResult)EffectAPIEffectTypeInternals.decodeComponents(map));
    }

    public static final Codec<DataComponentMap> CODEC = EffectAPIEffectTypes.codec(EffectAPIRegistries.EFFECT_TYPE);
    public static final Codec<DataComponentMap> VARIABLE_ALLOWED_CODEC = EffectAPIEffectTypes.variableAllowedCodec(EffectAPIRegistries.EFFECT_TYPE, EffectAPILootContextParamSets.ENTITY);
    public static final Codec<DataComponentMap> VARIABLE_ALLOWED_NETWORK_CODEC = EffectAPIEffectTypes.variableAllowedNetworkCodec(EffectAPIRegistries.EFFECT_TYPE, EffectAPILootContextParamSets.ENTITY);

    public static final EffectType<EffectAPIConditionalEffect<EntityAttributeEffect>> ATTRIBUTE = EffectType.<EffectAPIConditionalEffect<EntityAttributeEffect>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityAttributeEffect.CODEC, EffectAPILootContextParamSets.ENTITY))
            .build();
    public static final EffectType<DummyEffect> DUMMY = EffectType.<DummyEffect>builder()
            .codec(DummyEffect.codec(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("dummy")))
            .build();
    public static final EffectType<EntityResourceEffect<?>> RESOURCE = EffectType.<EntityResourceEffect<?>>builder()
            .codec(EntityResourceEffect.CODEC)
            .build();
    public static final EffectType<EffectAPIConditionalEffect<EntityTickEffect<?>>> TICK = EffectType.<EffectAPIConditionalEffect<EntityTickEffect<?>>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPILootContextParamSets.ENTITY))
            .build();
    public static final EffectType<EffectAPIConditionalEffect<EntityTriggerEffect>> TRIGGER = EffectType.<EffectAPIConditionalEffect<EntityTriggerEffect>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPILootContextParamSets.ENTITY))
            .build();

    public static void registerAll(RegistrationCallback<EffectType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("attribute"), ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("dummy"), DUMMY);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("tick"), TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("trigger"), TRIGGER);
    }
}
