package house.greenhouse.effectapi.entity.api;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.effect.DummyEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import house.greenhouse.effectapi.entity.api.effect.EntityAttributeEffect;
import house.greenhouse.effectapi.entity.api.effect.EntityEffectTypeBuilder;
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

import java.util.Optional;

public class EffectAPIEntityEffectTypes {
    public static final Codec<DataComponentMap> CODEC = EffectAPIEffectTypes.codec(EffectAPIEntityRegistries.EFFECT_TYPE);
    public static final Codec<DataComponentMap> VARIABLE_ALLOWED_CODEC = EffectAPIEffectTypes.variableAllowedCodec(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPIEntityLootContextParamSets.ENTITY);
    public static final Codec<DataComponentMap> VARIABLE_ALLOWED_NETWORK_CODEC = EffectAPIEffectTypes.variableAllowedNetworkCodec(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPIEntityLootContextParamSets.ENTITY);

    public static final EffectType<EffectAPIConditionalEffect<EntityAttributeEffect>, Entity> ENTITY_ATTRIBUTE = EntityEffectTypeBuilder.<EffectAPIConditionalEffect<EntityAttributeEffect>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityAttributeEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY))
            .build();
    public static final EffectType<DummyEffect<Entity>, Entity> DUMMY = EntityEffectTypeBuilder.<DummyEffect<Entity>>builder()
            .codec(DummyEffect.codec(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPI.asResource("dummy")))
            .build();
    public static final EffectType<EffectAPIConditionalEffect<EntityTickEffect<?>>, Entity> ENTITY_TICK = EntityEffectTypeBuilder.<EffectAPIConditionalEffect<EntityTickEffect<?>>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityTickEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY))
            .build();
    public static final EffectType<EntityResourceEffect<?>, Entity> ENTITY_RESOURCE = EntityEffectTypeBuilder.<EntityResourceEffect<?>>builder()
            .codec(EntityResourceEffect.CODEC)
            .build();
    public static final EffectType<EffectAPIConditionalEffect<EntityTriggerEffect>, Entity> ENTITY_TRIGGER = EntityEffectTypeBuilder.<EffectAPIConditionalEffect<EntityTriggerEffect>>builder()
            .codec(EffectAPIConditionalEffect.codec(EntityTriggerEffect.CODEC, EffectAPIEntityLootContextParamSets.ENTITY))
            .build();

    public static void registerAll(RegistrationCallback<EffectType<?, Entity>> callback) {
        callback.register(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPI.asResource("attribute"), ENTITY_ATTRIBUTE);
        callback.register(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPI.asResource("dummy"), DUMMY);
        callback.register(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPI.asResource("tick"), ENTITY_TICK);
        callback.register(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPI.asResource("resource"), ENTITY_RESOURCE);
        callback.register(EffectAPIEntityRegistries.EFFECT_TYPE, EffectAPI.asResource("trigger"), ENTITY_TRIGGER);
    }

    public static LootContext buildContext(Entity entity, ResourceLocation source) {
        if (entity.level().isClientSide())
            return null;

        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        params.withOptionalParameter(EffectAPILootContextParams.SOURCE, source);
        return new LootContext.Builder(params.create(EffectAPIEntityLootContextParamSets.ENTITY)).create(Optional.empty());
    }
}
