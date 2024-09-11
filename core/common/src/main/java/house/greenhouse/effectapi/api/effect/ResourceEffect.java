package house.greenhouse.effectapi.api.effect;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ResourceEffect<T> implements EffectAPIEffect {
    protected final ResourceLocation id;
    protected final Codec<T> resourceType;
    protected final T defaultValue;

    public ResourceEffect(ResourceLocation id, Codec<T> resourceType,
                          T defaultValue) {
        this.id = id;
        this.resourceType = resourceType;
        this.defaultValue = defaultValue;
        InternalResourceUtil.putInEffectMap(id, this);
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

    public static class ResourceEffectCodec<E extends ResourceEffect<?>> implements Codec<ResourceEffect<?>> {
        private static boolean registryPhase = false;
        private static final Map<ResourceLocation, ResourceEffect<?>> LOADED_EFFECTS = new HashMap<>();

        private final Function<Triple<ResourceLocation, Codec<Object>, Object>, E> constructor;

        protected ResourceEffectCodec(Function<Triple<ResourceLocation, Codec<Object>, Object>, E> constructor) {
            this.constructor = constructor;
        }


        public static <E extends ResourceEffect<?>> Codec<E> create(Function<Triple<ResourceLocation, Codec<Object>, Object>, E> constructor) {
            return new ResourceEffectCodec(constructor);
        }

        public static void setRegistryPhase(boolean value) {
            registryPhase = value;
        }

        public static void clearLoadedEffects() {
            LOADED_EFFECTS.clear();
        }

        @Override
        public <T> DataResult<Pair<ResourceEffect<?>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapLike = ops.getMap(input);
            if (mapLike.isError())
                return DataResult.error(() -> mapLike.error().get().message());

            var id = ResourceLocation.CODEC.decode(ops, mapLike.getOrThrow().get("id"));
            if (id.isError())
                return DataResult.error(() -> "Failed to decode 'id' field for `effectapi:resource` effect." + id.error().get().message());

            if (LOADED_EFFECTS.containsKey(id.getOrThrow().getFirst()))
                return DataResult.success(Pair.of(LOADED_EFFECTS.get(id.getOrThrow().getFirst()), input));

            var resourceType = EffectAPIRegistries.RESOURCE_TYPE.byNameCodec().decode(ops, mapLike.getOrThrow().get("resource_type"));
            if (resourceType.isError())
                return DataResult.error(() -> "Failed to decode 'resource_type' field for `effectapi:resource` effect." + resourceType.error().get().message());

            Codec<Object> resourceTypeCodec = (Codec<Object>) resourceType.getOrThrow().getFirst();
            var defaultValue = resourceTypeCodec.decode(ops, mapLike.getOrThrow().get("default_value"));
            if (defaultValue.isError())
                return DataResult.error(() -> "Failed to decode 'default_value' field for `effectapi:resource` effect." + resourceType.error().get().message());

            E effect = constructor.apply(Triple.of(id.getOrThrow().getFirst(), resourceTypeCodec, defaultValue.getOrThrow().getFirst()));
            if (registryPhase)
                LOADED_EFFECTS.put(id.getOrThrow().getFirst(), effect);
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
