package house.greenhouse.effectapi.api.attachment;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ResourcesAttachment(Map<ResourceLocation, ResourceEffect.ResourceHolder<Object>> resources) {
    public static final Codec<ResourcesAttachment> CODEC = new AttachmentCodec();

    public boolean hasResource(ResourceLocation id) {
        return resources.containsKey(id);
    }

    @Nullable
    public <T> T getValue(ResourceLocation id) {
        return (T) Optional.ofNullable(resources.get(id)).map(ResourceEffect.ResourceHolder::getValue).orElse(null);
    }

    public List<ResourceEffect.ResourceHolder<Object>> getAllFromSource(ResourceLocation source) {
        return resources.values().stream().filter(holder -> holder.getSource().equals(source)).toList();
    }

    public <T> T setValue(ResourceLocation id, T value, ResourceLocation source) {
        if (!resources.containsKey(id)) {
            var holder = new ResourceEffect.ResourceHolder<>(InternalResourceUtil.getEffectFromId(id), source);
            holder.setValue(value);
            resources.put(id, holder);
        } else {
            resources.get(id).setValue(value);
        }
        return value;
    }

    public void removeValue(ResourceLocation id) {
        if (!resources.containsKey(id))
            return;
        resources.remove(id);
    }

    @Nullable
    public <T> ResourceEffect.ResourceHolder<T> getResourceHolder(ResourceLocation id) {
        return (ResourceEffect.ResourceHolder<T>) resources.get(id);
    }

    public static class AttachmentCodec implements Codec<ResourcesAttachment> {
        protected AttachmentCodec() {}

        @Override
        public <T> DataResult<Pair<ResourcesAttachment, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> map = ops.getMap(input);
            if (map.isError()) {
                return DataResult.error(() -> map.error().get().message());
            }

            Map<ResourceLocation, ResourceEffect.ResourceHolder<Object>> finalMap = new HashMap<>();
            List<String> errors = new ArrayList<>();

            for (Pair<T, T> entry : map.getOrThrow().entries().toList()) {
                DataResult<Pair<ResourceLocation, T>> id = ResourceLocation.CODEC.decode(ops, entry.getFirst());
                DataResult<MapLike<T>> mapLike = ops.getMap(entry.getSecond());
                if (id.isError()) {
                    errors.add("Failed to parse id \"" + id + "\" in attachment. (Skipping). " + id.error().get().message());
                    continue;
                }
                if (mapLike.isError()) {
                    errors.add("Attempt to deserialize resource \"" + id + "\" that is not a map. (Skipping). " + id.error().get().message());
                    continue;
                }

                ResourceEffect<Object> effect = InternalResourceUtil.getEffectFromId(id.getOrThrow().getFirst());

                if (effect == null) {
                    errors.add("Could not find resource effect with id '" + id.getOrThrow() + "'. (Skipping).");
                    continue;
                }

                var value = effect.getDefaultValue();
                if (mapLike.getOrThrow().get("value") != null) {
                    var newValue = effect.getResourceTypeCodec().decode(ops, mapLike.getOrThrow().get("value"));
                    if (newValue.isError()) {
                        errors.add("Failed to decode value to attachment. (Skipping). " + newValue.error().get().message());
                        continue;
                    }
                    value = newValue.getOrThrow().getFirst();
                }

                var newSource = ResourceLocation.CODEC.decode(ops, mapLike.getOrThrow().get("source"));
                if (newSource.isError()) {
                    errors.add("Failed to decode value to attachment. (Skipping). " + newSource.error().get().message());
                    continue;
                }

                ResourceEffect.ResourceHolder<Object> holder = new ResourceEffect.ResourceHolder<>(effect, newSource.getOrThrow().getFirst());
                holder.setValue(value);

                finalMap.put(id.getOrThrow().getFirst(), holder);
            }

            for (String error : errors) {
                EffectAPI.LOG.error(error);
            }

            return DataResult.success(Pair.of(new ResourcesAttachment(finalMap), input));
        }

        @Override
        public <T> DataResult<T> encode(ResourcesAttachment input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();
            for (Map.Entry<ResourceLocation, ResourceEffect.ResourceHolder<Object>> entry : input.resources.entrySet()) {
                try {
                    Map<T, T> innerMap = new HashMap<>();
                    innerMap.put(ops.createString("value"), entry.getValue().getEffect().getResourceTypeCodec().encodeStart(ops, entry.getValue().getValue()).getOrThrow());
                    innerMap.put(ops.createString("source"), ResourceLocation.CODEC.encodeStart(ops, entry.getValue().getSource()).getOrThrow());
                    map.put(ResourceLocation.CODEC.encodeStart(ops, entry.getKey()).getOrThrow(), ops.createMap(innerMap));
                } catch (Exception ex) {
                    EffectAPI.LOG.error("Failed to encode resource '{}' to attachment. (Skipping).", entry.getKey(), ex);
                }
            }
            return DataResult.success(ops.createMap(map));
        }
    }
}
