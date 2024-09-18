package house.greenhouse.effectapi.impl.attachment;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
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

public record ResourcesAttachmentImpl(Map<Holder<Resource<Object>>, Pair<ResourceEffect.ResourceHolder<Object>, Set<ResourceLocation>>> resources) implements ResourcesAttachment {
    public static final Codec<ResourcesAttachment> CODEC = new AttachmentCodec();

    @Override
    public boolean isEmpty() {
        return resources.isEmpty();
    }

    public <T> boolean hasResource(Holder<Resource<T>> resource) {
        return resources.containsKey(resource);
    }

    @Override
    public <T> boolean hasSource(Holder<Resource<T>> resource, ResourceLocation source) {
        if (!resources.containsKey(resource))
            return false;
        return resources.get(resource).getSecond().contains(source);
    }

    @Override
    public <T> boolean hasSourceFromMod(Holder<Resource<T>> resource, String modId) {
        if (!resources.containsKey(resource))
            return false;
        return resources.get(resource).getSecond().stream().anyMatch(source -> source.getNamespace().equals(modId));
    }

    @Nullable
    public <T> T getValue(Holder<Resource<T>> id) {
        return (T) Optional.ofNullable(resources.get(id)).map(pair -> pair.getFirst().getValue()).orElse(null);
    }

    public <T> T setValue(Holder<Resource<T>> resource, T value, ResourceLocation source) {
        if (!resources.containsKey(resource)) {
            var holder = new ResourceEffect.ResourceHolder<>(resource);
            holder.setValue(value);
            Set<ResourceLocation> sources = new HashSet<>();
            sources.add(source);
            resources.put((Holder) resource, Pair.of((ResourceEffect.ResourceHolder<Object>) holder, sources));
        } else {
            resources.get(resource).getFirst().setValue(value);
            if (source != null)
                resources.get(resource).getSecond().add(source);
        }
        return value;
    }

    public <T> void removeValue(Holder<Resource<T>> resource, ResourceLocation source) {
        if (!resources.containsKey(resource))
            return;
        resources.get(resource).getSecond().remove(source);
        if (resources.get(resource).getSecond().isEmpty())
            resources.remove(resource);
    }

    @Nullable
    public <T> ResourceEffect.ResourceHolder<T> getResourceHolder(Holder<Resource<T>> resource) {
        return resources.containsKey(resource) ? (ResourceEffect.ResourceHolder<T>) resources.get(resource).getFirst() : null;
    }

    @Nullable
    public <T> Collection<ResourceLocation> getSources(Holder<Resource<T>> resource) {
        return resources.containsKey(resource) ? resources.get(resource).getSecond() : ImmutableSet.of();
    }

    public static class AttachmentCodec implements Codec<ResourcesAttachment> {
        protected AttachmentCodec() {}

        @Override
        public <T> DataResult<Pair<ResourcesAttachment, T>> decode(DynamicOps<T> ops, T input) {
            if (!(ops instanceof RegistryOps<T> registryOps))
                return DataResult.error(() -> "Cannot decode resources attachment from a non registry context.");

            DataResult<MapLike<T>> map = registryOps.getMap(input);
            if (map.isError()) {
                return DataResult.error(() -> map.error().get().message());
            }

            Map<Holder<Resource<Object>>, Pair<ResourceEffect.ResourceHolder<Object>, Set<ResourceLocation>>> finalMap = new HashMap<>();
            List<String> errors = new ArrayList<>();

            for (Pair<T, T> entry : map.getOrThrow().entries().toList()) {
                DataResult<Pair<Holder<Resource<?>>, T>> resource = Resource.CODEC.decode(ops, entry.getFirst());
                DataResult<MapLike<T>> mapLike = registryOps.getMap(entry.getSecond());
                if (resource.isError()) {
                    errors.add("Failed to parse resource \"" + resource.error().get().getOrThrow().getSecond() + "\" in attachment. (Skipping). " + resource.error().get().message());
                    continue;
                }
                if (mapLike.isError()) {
                    errors.add("Attempted to deserialize resource \"" + resource.getOrThrow().getFirst().unwrapKey().get().location() + "\" that is not a map. (Skipping). " + mapLike.error().get().message());
                    continue;
                }

                var value = resource.getOrThrow().getFirst().value().defaultValue();
                if (mapLike.getOrThrow().get("value") != null) {
                    var newValue = resource.getOrThrow().getFirst().value().dataType().codec().decode(registryOps, mapLike.getOrThrow().get("value"));
                    if (newValue.isError()) {
                        errors.add("Failed to decode value of resource \"" + resource.getOrThrow().getFirst().unwrapKey().get().location() + "\" to attachment. (Skipping). " + newValue.error().get().message());
                        continue;
                    }
                    value = newValue.getOrThrow().getFirst();
                }

                var newSources = ResourceLocation.CODEC.listOf().decode(registryOps, mapLike.getOrThrow().get("sources"));
                if (newSources.isError()) {
                    errors.add("Failed to decode sources of resource \"" + resource.getOrThrow().getFirst().unwrapKey().get().location() + "\" to attachment. (Skipping). " + newSources.error().get().message());
                    continue;
                }

                ResourceEffect.ResourceHolder<Object> holder = new ResourceEffect.ResourceHolder(resource.getOrThrow().getFirst());
                holder.setValue(value);

                finalMap.put((Holder) resource.getOrThrow().getFirst(), Pair.of(holder, new HashSet<>(newSources.getOrThrow().getFirst())));
            }

            for (String error : errors) {
                EffectAPI.LOG.error(error);
            }

            return DataResult.success(Pair.of(new ResourcesAttachmentImpl(finalMap), input));
        }

        @Override
        public <T> DataResult<T> encode(ResourcesAttachment input, DynamicOps<T> ops, T prefix) {
            if (!(ops instanceof RegistryOps<T> registryOps))
                return DataResult.error(() -> "Cannot decode resources attachment from a non registry context.");

            Map<T, T> map = new HashMap<>();
            for (Map.Entry<Holder<Resource<Object>>, Pair<ResourceEffect.ResourceHolder<Object>, Set<ResourceLocation>>> entry : ((ResourcesAttachmentImpl)input).resources.entrySet()) {
                try {
                    Map<T, T> innerMap = new HashMap<>();
                    innerMap.put(registryOps.createString("value"), entry.getValue().getFirst().getResource().value().dataType().codec().encodeStart(registryOps, entry.getValue().getFirst().getValue()).getOrThrow());
                    innerMap.put(registryOps.createString("sources"), ResourceLocation.CODEC.listOf().encodeStart(registryOps, new ArrayList<>(entry.getValue().getSecond())).getOrThrow());
                    map.put((T) Resource.CODEC.encodeStart(registryOps, (Holder) entry.getKey()).getOrThrow(), registryOps.createMap(innerMap));
                } catch (Exception ex) {
                    EffectAPI.LOG.error("Failed to encode resource '{}' to attachment. (Skipping).", entry.getKey(), ex);
                }
            }
            return DataResult.success(registryOps.createMap(map));
        }
    }
}
