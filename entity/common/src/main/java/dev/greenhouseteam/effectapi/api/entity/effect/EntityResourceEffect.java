package dev.greenhouseteam.effectapi.api.entity.effect;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.entity.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.api.entity.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParams;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record EntityResourceEffect<T>(ResourceLocation id, Codec<T> resourceType,
                                      T defaultValue) implements EffectAPIEffect {
    public static final Codec<EntityResourceEffect<?>> CODEC = new EffectCodec();
    private static final Map<ResourceLocation, EntityResourceEffect<?>> ID_TO_EFFECT_MAP = new HashMap<>();

    public EntityResourceEffect(ResourceLocation id, Codec<T> resourceType,
                                T defaultValue) {
        this.id = id;
        this.resourceType = resourceType;
        this.defaultValue = defaultValue;
        ID_TO_EFFECT_MAP.put(id, this);
    }

    public static Map<ResourceLocation, EntityResourceEffect<?>> getIdMap() {
        return Map.copyOf(ID_TO_EFFECT_MAP);
    }

    public static void clearEffectMap() {
        ID_TO_EFFECT_MAP.clear();
    }

    @Override
    public void onAdded(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        T value = EffectAPIEntity.getHelper().setResource(entity, id, defaultValue, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
        EffectAPI.getHelper().sendClientboundTracking(new ChangeResourceClientboundPacket<>(entity.getId(), this, Optional.ofNullable(lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE)), Optional.of(value)), entity);
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        EffectAPIEntity.getHelper().removeResource(entity, id, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
        EffectAPI.getHelper().sendClientboundTracking(new ChangeResourceClientboundPacket<>(entity.getId(), this, Optional.empty(), Optional.empty()), entity);
    }

    @Override
    public void onRefreshed(LootContext context) {}

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.RESOURCE;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPILootContextParamSets.ENTITY;
    }

    @Nullable
    public static <T> EntityResourceEffect<T> getEffectFromId(ResourceLocation id) {
        return (EntityResourceEffect<T>) ID_TO_EFFECT_MAP.get(id);
    }

    public ResourceLocation getId() {
        return id;
    }

    public Codec<T> getResourceTypeCodec() {
        return resourceType;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public static class ResourceHolder<T> {

        private final EntityResourceEffect<T> effect;
        private T value;
        private final ResourceLocation source;

        public ResourceHolder(EntityResourceEffect<T> effect, ResourceLocation source) {
            this.effect = effect;
            this.value = effect.defaultValue;
            this.source = source;
        }

        public EntityResourceEffect<T> getEffect() {
            return effect;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public ResourceLocation getSource() {
            return source;
        }
    }

    public static class EffectCodec implements Codec<EntityResourceEffect<?>> {
        private static boolean registryPhase = false;
        private static final List<ResourceLocation> LOADED_IDS = new ArrayList<>();

        protected EffectCodec() {

        }


        public static void setRegistryPhase(boolean value) {
            registryPhase = value;
        }

        public static void clearLoadedIds() {
            LOADED_IDS.clear();
        }

        @Override
        public <T> DataResult<Pair<EntityResourceEffect<?>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapLike = ops.getMap(input);
            if (mapLike.isError())
                return DataResult.error(() -> mapLike.error().get().message());

            var id = ResourceLocation.CODEC.decode(ops, mapLike.getOrThrow().get("id"));
            if (id.isError())
                return DataResult.error(() -> "Failed to decode 'id' field for `effectapi:resource` effect." + id.error().get().message());

            if (LOADED_IDS.contains(id.getOrThrow().getFirst()))
                return DataResult.error(() -> "Attempted to register duplicate resource ID '" + id.getOrThrow().getFirst() + "'.");

            var resourceType = EffectAPIRegistries.RESOURCE_TYPE.byNameCodec().decode(ops, mapLike.getOrThrow().get("resource_type"));
            if (resourceType.isError())
                return DataResult.error(() -> "Failed to decode 'resource_type' field for `effectapi:resource` effect." + resourceType.error().get().message());

            Codec<Object> resourceTypeCodec = (Codec<Object>) resourceType.getOrThrow().getFirst();
            var defaultValue = resourceTypeCodec.decode(ops, mapLike.getOrThrow().get("default_value"));
            if (defaultValue.isError())
                return DataResult.error(() -> "Failed to decode 'default_value' field for `effectapi:resource` effect." + resourceType.error().get().message());

            EntityResourceEffect<?> effect = new EntityResourceEffect<>(id.getOrThrow().getFirst(), resourceTypeCodec, defaultValue.getOrThrow().getFirst());
            if (registryPhase)
                LOADED_IDS.add(id.getOrThrow().getFirst());
            return DataResult.success(Pair.of(effect, input));
        }

        @Override
        public <T> DataResult<T> encode(EntityResourceEffect<?> input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("id"), ResourceLocation.CODEC.encodeStart(ops, input.id).getOrThrow());
            map.put(ops.createString("resource_type"), EffectAPIRegistries.RESOURCE_TYPE.byNameCodec().encodeStart(ops, input.getResourceTypeCodec()).getOrThrow());
            map.put(ops.createString("default_value"), ((Codec<Object>)input.getResourceTypeCodec()).encodeStart(ops, input.defaultValue).getOrThrow());
            return DataResult.success(ops.createMap(map));
        }
    }
}
