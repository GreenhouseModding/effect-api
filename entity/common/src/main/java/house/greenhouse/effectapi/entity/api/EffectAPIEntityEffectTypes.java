package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.effect.DummyEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import house.greenhouse.effectapi.entity.api.effect.EntityAttributeEffect;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityTickEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityTriggerEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class EffectAPIEntityEffectTypes {
    public static final Codec<DataComponentMap> CODEC = EffectAPIEffectTypes.codec(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE);
    public static final Codec<DataComponentMap> VARIABLE_ALLOWED_CODEC = EffectAPIEffectTypes.variableAllowedCodec(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPIEntityRegistries.VARIABLE, EffectAPIEntityLootContextParamSets.ENTITY);
    public static final Codec<DataComponentMap> VARIABLE_ALLOWED_NETWORK_CODEC = EffectAPIEffectTypes.variableAllowedNetworkCodec(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPIEntityRegistries.VARIABLE, EffectAPIEntityLootContextParamSets.ENTITY);

    public static final DataComponentType<EffectAPIConditionalEffect<EntityAttributeEffect>> ENTITY_ATTRIBUTE = DataComponentType.<EffectAPIConditionalEffect<EntityAttributeEffect>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityAttributeEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY))
            .build();
    public static final DataComponentType<DummyEffect> DUMMY = DataComponentType.<DummyEffect>builder()
            .persistent(DummyEffect.codec(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("dummy"), EffectAPIEntityLootContextParamSets.ENTITY))
            .build();
    public static final DataComponentType<EffectAPIConditionalEffect<EntityTickEffect<?>>> ENTITY_TICK = DataComponentType.<EffectAPIConditionalEffect<EntityTickEffect<?>>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY))
            .build();
    public static final DataComponentType<EntityResourceEffect<?>> ENTITY_RESOURCE = DataComponentType.<EntityResourceEffect<?>>builder()
            .persistent(EntityResourceEffect.CODEC)
            .build();
    public static final DataComponentType<EffectAPIConditionalEffect<EntityTriggerEffect>> ENTITY_TRIGGER = DataComponentType.<EffectAPIConditionalEffect<EntityTriggerEffect>>builder()
            .persistent(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY))
            .build();

    public static void registerAll(RegistrationCallback<DataComponentType<?>> callback) {
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("attribute"), ENTITY_ATTRIBUTE);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("dummy"), DUMMY);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("tick"), ENTITY_TICK);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("resource"), ENTITY_RESOURCE);
        callback.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPI.asResource("trigger"), ENTITY_TRIGGER);
    }

    private static Map<DataComponentType<?>, BiFunction<Entity, ResourceLocation, LootContext>> LOOT_CONTEXT_BUILDERS = new HashMap<>();

    public static LootContext buildContext(DataComponentType<?> type, Entity entity, ResourceLocation source) {
        if (entity.level().isClientSide())
            return null;

        if (LOOT_CONTEXT_BUILDERS.containsKey(type)) {
            return LOOT_CONTEXT_BUILDERS.get(type).apply(entity, source);
        }

        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        params.withOptionalParameter(EffectAPILootContextParams.SOURCE, source);
        return new LootContext.Builder(params.create(EffectAPIEntityLootContextParamSets.ENTITY)).create(Optional.empty());
    }

    public static void registerLootContextBuilder(DataComponentType<?> type, BiFunction<Entity, ResourceLocation, LootContext> builder) {
        LOOT_CONTEXT_BUILDERS.put(type, builder);
    }
}
