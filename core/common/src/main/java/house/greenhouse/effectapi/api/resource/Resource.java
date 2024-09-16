package house.greenhouse.effectapi.api.resource;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.variable.DataType;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;

import java.util.HashMap;
import java.util.Map;

public record Resource<T>(DataType<T> dataType, T defaultValue) {
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

            var dataType = EffectAPIRegistries.DATA_TYPE.byNameCodec().decode(ops, mapLike.getOrThrow().get("data_type"));
            if (dataType.isError())
                return DataResult.error(() -> "Failed to decode 'data_type' field for resource effect." + dataType.error().get().message());

            DataType<Object> resourceTypeCodec = (DataType<Object>) dataType.getOrThrow().getFirst();
            var defaultValue = resourceTypeCodec.codec().decode(ops, mapLike.getOrThrow().get("default_value"));
            if (defaultValue.isError())
                return DataResult.error(() -> "Failed to decode 'default_value' field for resource effect." + defaultValue.error().get().message());

            Resource<?> resource = new Resource<>(resourceTypeCodec, defaultValue.getOrThrow().getFirst());
            return DataResult.success(Pair.of(resource, input));
        }

        @Override
        public <T> DataResult<T> encode(Resource<?> input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("data_type"), EffectAPIRegistries.DATA_TYPE.byNameCodec().encodeStart(ops, input.dataType).getOrThrow());
            map.put(ops.createString("default_value"), ((DataType<Object>)input.dataType).codec().encodeStart(ops, input.defaultValue).getOrThrow());
            return DataResult.success(ops.createMap(map));
        }
    }
}
