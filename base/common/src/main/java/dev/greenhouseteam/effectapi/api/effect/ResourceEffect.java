package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class ResourceEffect<T> implements EffectAPIEffect {

    private ResourceLocation id;
    private Codec<T> resourceType;
    private T defaultValue;

    public ResourceEffect(ResourceLocation id, Codec<T> resourceType,
                          T defaultValue) {
        this.id = id;
        this.resourceType = resourceType;
        this.defaultValue = defaultValue;
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
        private final ResourceLocation source;

        public ResourceHolder(ResourceEffect<T> effect, ResourceLocation source) {
            this.effect = effect;
            this.value = effect.defaultValue;
            this.source = source;
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

        public ResourceLocation getSource() {
            return source;
        }
    }

    public static class ResourceEffectCodec<E extends ResourceEffect, V> implements Codec<ResourceEffect<?>> {
        private static boolean registryPhase = false;
        private static final List<ResourceLocation> LOADED_IDS = new ArrayList<>();

        private Function<Triple<ResourceLocation, Codec<Object>, Object>, E> constructor;

        protected ResourceEffectCodec(Function<Triple<ResourceLocation, Codec<Object>, Object>, E> constructor) {
            this.constructor = constructor;
        }


        public static <E extends ResourceEffect<Object>> ResourceEffectCodec create(Function<Triple<ResourceLocation, Codec<Object>, Object>, E> constructor) {
            return new ResourceEffectCodec(constructor);
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

            ResourceEffect<Object> effect = constructor.apply(Triple.of(id.getOrThrow().getFirst(), resourceTypeCodec, defaultValue.getOrThrow().getFirst()));
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
