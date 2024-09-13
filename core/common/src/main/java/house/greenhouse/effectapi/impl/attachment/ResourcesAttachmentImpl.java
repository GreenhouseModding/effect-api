package house.greenhouse.effectapi.impl.attachment;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record ResourcesAttachmentImpl(Map<ResourceLocation, Pair<ResourceEffect.ResourceHolder<Object>, Set<ResourceLocation>>> resources) implements ResourcesAttachment {
    public static final Codec<ResourcesAttachment> CODEC = new AttachmentCodec();

    @Override
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    public boolean hasResource(ResourceLocation id) {
        return resources.containsKey(id);
    }

    @Nullable
    public <T> T getValue(ResourceLocation id) {
        return (T) Optional.ofNullable(resources.get(id)).map(pair -> pair.getFirst().getValue()).orElse(null);
    }

    public <T> T setValue(ResourceLocation id, T value, ResourceLocation source) {
        if (!resources.containsKey(id)) {
            var holder = new ResourceEffect.ResourceHolder<>(InternalResourceUtil.getEffectFromId(id));
            holder.setValue(value);
            Set<ResourceLocation> sources = new HashSet<>();
            sources.add(source);
            resources.put(id, Pair.of(holder, sources));
        } else
            resources.get(id).getSecond().add(source);
        return value;
    }

    public void removeValue(ResourceLocation id, ResourceLocation source) {
        if (!resources.containsKey(id))
            return;
        resources.get(id).getSecond().remove(source);
        if (resources.get(id).getSecond().isEmpty())
            resources.remove(id);
    }

    @Nullable
    public <T> ResourceEffect.ResourceHolder<T> getResourceHolder(ResourceLocation id) {
        return resources.containsKey(id) ? (ResourceEffect.ResourceHolder<T>) resources.get(id).getFirst() : null;
    }

    @Nullable
    public Collection<ResourceLocation> getSources(ResourceLocation id) {
        return resources.containsKey(id) ? resources.get(id).getSecond() : ImmutableSet.of();
    }

    public static class AttachmentCodec implements Codec<ResourcesAttachment> {
        protected AttachmentCodec() {}

        @Override
        public <T> DataResult<Pair<ResourcesAttachment, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<MapLike<T>> map = ops.getMap(input);
            if (map.isError()) {
                return DataResult.error(() -> map.error().get().message());
            }

            Map<ResourceLocation, Pair<ResourceEffect.ResourceHolder<Object>, Set<ResourceLocation>>> finalMap = new HashMap<>();
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
                        errors.add("Failed to decode value of resource \"" + id.getOrThrow() + "\" to attachment. (Skipping). " + newValue.error().get().message());
                        continue;
                    }
                    value = newValue.getOrThrow().getFirst();
                }

                var newSources = ResourceLocation.CODEC.listOf().decode(ops, mapLike.getOrThrow().get("sources"));
                if (newSources.isError()) {
                    errors.add("Failed to decode sources of resource \"" + id.getOrThrow() + "\" to attachment. (Skipping). " + newSources.error().get().message());
                    continue;
                }

                ResourceEffect.ResourceHolder<Object> holder = new ResourceEffect.ResourceHolder<>(effect);
                holder.setValue(value);

                finalMap.put(id.getOrThrow().getFirst(), Pair.of(holder, new HashSet<>(newSources.getOrThrow().getFirst())));
            }

            for (String error : errors) {
                EffectAPI.LOG.error(error);
            }

            return DataResult.success(Pair.of(new ResourcesAttachmentImpl(finalMap), input));
        }

        @Override
        public <T> DataResult<T> encode(ResourcesAttachment input, DynamicOps<T> ops, T prefix) {
            Map<T, T> map = new HashMap<>();
            for (Map.Entry<ResourceLocation, Pair<ResourceEffect.ResourceHolder<Object>, Set<ResourceLocation>>> entry : ((ResourcesAttachmentImpl)input).resources.entrySet()) {
                try {
                    Map<T, T> innerMap = new HashMap<>();
                    innerMap.put(ops.createString("value"), entry.getValue().getFirst().getEffect().getResourceTypeCodec().encodeStart(ops, entry.getValue().getFirst().getValue()).getOrThrow());
                    innerMap.put(ops.createString("sources"), ResourceLocation.CODEC.listOf().encodeStart(ops, new ArrayList<>(entry.getValue().getSecond())).getOrThrow());
                    map.put(ResourceLocation.CODEC.encodeStart(ops, entry.getKey()).getOrThrow(), ops.createMap(innerMap));
                } catch (Exception ex) {
                    EffectAPI.LOG.error("Failed to encode resource '{}' to attachment. (Skipping).", entry.getKey(), ex);
                }
            }
            return DataResult.success(ops.createMap(map));
        }
    }
}
