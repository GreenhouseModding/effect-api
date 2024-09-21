package house.greenhouse.effectapi.impl.registry;

import house.greenhouse.effectapi.api.EffectAPILootContextContents;
import house.greenhouse.effectapi.api.effect.DummyEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.effect.EntityAttributeEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.impl.effect.EntityTickEffect;
import house.greenhouse.effectapi.impl.effect.EntityTriggerEffect;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIEffectTypes {
    public static final EffectType<EffectAPIConditionalEffect<EntityAttributeEffect>> ATTRIBUTE = EffectType.<EffectAPIConditionalEffect<EntityAttributeEffect>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityAttributeEffect.CODEC, EffectAPILootContextContents.ENTITY))
            .build();
    public static final EffectType<DummyEffect> DUMMY = EffectType.<DummyEffect>builder()
            .codec(DummyEffect.codec(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("dummy")))
            .build();
    public static final EffectType<EntityResourceEffect<?>> RESOURCE = EffectType.<EntityResourceEffect<?>>builder()
            .codec(EntityResourceEffect.CODEC)
            .build();
    public static final EffectType<EffectAPIConditionalEffect<EntityTickEffect<?>>> TICK = EffectType.<EffectAPIConditionalEffect<EntityTickEffect<?>>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPILootContextContents.ENTITY))
            .build();
    public static final EffectType<EffectAPIConditionalEffect<EntityTriggerEffect>> TRIGGER = EffectType.<EffectAPIConditionalEffect<EntityTriggerEffect>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPILootContextContents.ENTITY))
            .build();

    public static void registerAll(RegistrationCallback<EffectType<?>> callback) {
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("attribute"), ATTRIBUTE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("dummy"), DUMMY);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("resource"), RESOURCE);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("tick"), TICK);
        callback.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPI.asResource("trigger"), TRIGGER);
    }
}
