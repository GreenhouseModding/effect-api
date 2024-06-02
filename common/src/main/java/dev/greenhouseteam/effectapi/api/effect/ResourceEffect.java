package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import dev.greenhouseteam.effectapi.api.EffectAPIEffects;
import dev.greenhouseteam.effectapi.api.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceEffect<T> extends Effect {
    public static final Codec<ResourceEffect<?>> CODEC = new EffectCodec();
    private static final Map<ResourceLocation, ResourceEffect<?>> ID_TO_EFFECT_MAP = new HashMap<>();

    private final ResourceLocation id;
    private final Codec<T> resourceType;
    private T defaultValue;

    public ResourceEffect(ResourceLocation id, Codec<T> resourceType, T defaultValue) {
        this.id = id;
        this.resourceType = resourceType;
        this.defaultValue = defaultValue;
        ID_TO_EFFECT_MAP.put(id, this);
    }

    public static void clearEffectMap() {
        ID_TO_EFFECT_MAP.clear();
    }

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEffects.RESOURCE;
    }

    @Override
    public void onAdded(Entity entity) {
        T value = EffectAPI.getHelper().setResource(entity, id, defaultValue);
        EffectAPI.getHelper().sendClientboundTracking(new ChangeResourceClientboundPacket<>(entity.getId(), this, Optional.of(value)), entity);
    }

    @Override
    public void onRemoved(Entity entity) {
        EffectAPI.getHelper().removeResource(entity, id);
        EffectAPI.getHelper().sendClientboundTracking(new ChangeResourceClientboundPacket<>(entity.getId(), this, Optional.empty()), entity);
    }

    @Override
    public Codec<? extends Effect> codec() {
        return CODEC;
    }

    @Nullable
    public static <T> ResourceEffect<T> getEffectFromId(ResourceLocation id) {
        return (ResourceEffect<T>) ID_TO_EFFECT_MAP.get(id);
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

        private final ResourceEffect<T> effect;
        private T value;

        public ResourceHolder(ResourceEffect<T> effect) {
            this.effect = effect;
            this.value = effect.defaultValue;
        }

        public ResourceEffect<T> getEffect() {
            return effect;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    public static class EffectCodec implements Codec<ResourceEffect<?>> {
        private static boolean registryPhase = true;
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
        public <T> DataResult<Pair<ResourceEffect<?>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapLike = ops.getMap(input);
            if (mapLike.isError())
                return DataResult.error(() -> mapLike.error().get().message());

            DataResult<Pair<ResourceLocation, T>> id = ResourceLocation.CODEC.decode(ops, mapLike.getOrThrow().get("id"));
            if (id.isError())
                return DataResult.error(() -> "Failed to decode 'id' field for `effectapi:resource` effect." + id.error().get().message());

            if (LOADED_IDS.contains(id.getOrThrow().getFirst()))
                return DataResult.error(() -> "Attempted to register duplicate resource ID '" + id.getOrThrow().getFirst() + "'.");

            DataResult<Pair<Codec<?>, T>> resourceType = EffectAPIRegistries.RESOURCE_TYPE.byNameCodec().decode(ops, mapLike.getOrThrow().get("resource_type"));
            if (resourceType.isError())
                return DataResult.error(() -> "Failed to decode 'resource_type' field for `effectapi:resource` effect." + resourceType.error().get().message());

            Codec<Object> resourceTypeCodec = (Codec<Object>) resourceType.getOrThrow().getFirst();
            DataResult<Pair<Object, T>> defaultValue = resourceTypeCodec.decode(ops, mapLike.getOrThrow().get("default_value"));
            if (defaultValue.isError())
                return DataResult.error(() -> "Failed to decode 'default_value' field for `effectapi:resource` effect." + resourceType.error().get().message());

            ResourceEffect<?> effect = new ResourceEffect<>(id.getOrThrow().getFirst(), resourceTypeCodec, defaultValue.getOrThrow().getFirst());
            if (registryPhase)
                LOADED_IDS.add(id.getOrThrow().getFirst());
            return DataResult.success(Pair.of(effect, input));
        }

        @Override
        public <T> DataResult<T> encode(ResourceEffect<?> input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("id"), ResourceLocation.CODEC.encodeStart(ops, input.id).getOrThrow());
            map.put(ops.createString("resource_type"), EffectAPIRegistries.RESOURCE_TYPE.byNameCodec().encodeStart(ops, input.getResourceTypeCodec()).getOrThrow());
            map.put(ops.createString("default_value"), ((Codec<Object>)input.getResourceTypeCodec()).encodeStart(ops, input.defaultValue).getOrThrow());
            return DataResult.success(ops.createMap(map));
        }
    }
}
