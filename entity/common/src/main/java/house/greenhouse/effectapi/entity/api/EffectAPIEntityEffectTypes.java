package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.effect.DummyEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.VariableHolder;
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
    public static final Codec<DataComponentMap> CODEC = EffectAPIEffectTypes.codec(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPIEntityLootContextParamSets.ENTITY);
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
}
