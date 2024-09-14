package house.greenhouse.effectapi.api.resource;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record Resource<T>(Codec<T> typeCodec, T defaultValue) {
    public static final Codec<Resource<?>> DIRECT_CODEC = new ResourceCodec();
    public static final Codec<Holder<Resource<?>>> CODEC = RegistryFixedCodec.create(EffectAPIRegistryKeys.RESOURCE);

    public static class ResourceCodec implements Codec<Resource<?>> {

        protected ResourceCodec() {
        }

        @Override
        public <T> DataResult<Pair<Resource<?>, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> mapLike = ops.getMap(input);
            if (mapLike.isError())
                return DataResult.error(() -> mapLike.error().get().message());

            var resourceType = EffectAPIRegistries.VARIABLE_TYPE.byNameCodec().decode(ops, mapLike.getOrThrow().get("resource_type"));
            if (resourceType.isError())
                return DataResult.error(() -> "Failed to decode 'resource_type' field for `effect_api:resource` effect." + resourceType.error().get().message());

            Codec<Object> resourceTypeCodec = (Codec<Object>) resourceType.getOrThrow().getFirst();
            var defaultValue = resourceTypeCodec.decode(ops, mapLike.getOrThrow().get("default_value"));
            if (defaultValue.isError())
                return DataResult.error(() -> "Failed to decode 'default_value' field for `effect_api:resource` effect." + resourceType.error().get().message());

            Resource<?> resource = new Resource<>(resourceTypeCodec, defaultValue.getOrThrow().getFirst());
            return DataResult.success(Pair.of(resource, input));
        }

        @Override
        public <T> DataResult<T> encode(Resource<?> input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("resource_type"), EffectAPIRegistries.VARIABLE_TYPE.byNameCodec().encodeStart(ops, input.typeCodec).getOrThrow());
            map.put(ops.createString("default_value"), ((Codec<Object>)input.typeCodec).encodeStart(ops, input.defaultValue).getOrThrow());
            return DataResult.success(ops.createMap(map));
        }
    }
}
