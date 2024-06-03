package dev.greenhouseteam.effectapi.api.attachment;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPITickingEffect;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncEffectsClientboundPacket;
import dev.greenhouseteam.effectapi.api.util.EffectUtil;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.stream.Collectors;

public class EffectsAttachment {
    public static final ResourceLocation ID = EffectAPI.asResource("effects");
    public static final Codec<EffectsAttachment> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.unboundedMap(ResourceLocation.CODEC, EffectAPIEffectTypes.CODEC).optionalFieldOf("effects", Map.of()).forGetter(attachment -> attachment.allComponents.entrySet().stream().map(entry -> Pair.of(entry.getKey().id(), entry.getValue())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))),
            EffectSource.CODEC.listOf().optionalFieldOf("sources", List.of()).forGetter(attachment -> attachment.allComponents.keySet().stream().toList())
    ).apply(inst, (map, sources) -> {
        var attachment = new EffectsAttachment();
        attachment.setComponents(loadMap(map, sources), DataComponentMap.EMPTY);
        return attachment;
    }));

    private Map<EffectSource, DataComponentMap> allComponents = new HashMap<>();
    private DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;
    private Entity provider;

    public EffectsAttachment() {}

    public void init(Entity entity) {
        this.provider = entity;
    }

    public boolean isEmpty() {
        return allComponents.isEmpty();
    }

    public void getEffects(DataComponentType<?> type) {
        activeComponents.getOrDefault(type, List.of());
    }

    public void tick() {
        updateActiveComponents();
        for (var entry : activeComponents) {
            if (entry.type() == EffectAPIEffectTypes.ENTITY_TICK && entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> ((EffectAPITickingEffect)((EffectAPIConditionalEffect)effect).effect()).tick(EffectAPIEffect.createEntityOnlyContext(provider)));
        }
    }

    public void onRespawn() {
        allComponents.keySet().removeIf(effectSource -> !effectSource.persistsOnRespawn());
        combineComponents();
    }

    public void refresh() {
        for (var entry : activeComponents) {
            if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> ((EffectAPIEffect)effect).onRemoved(EffectAPIEffect.createEntityOnlyContext(provider)));
        }
    }

    private void updateActiveComponents() {
        DataComponentMap previous = activeComponents;
        DataComponentMap potential = EffectUtil.getActive(provider, combinedComponents);
        if (EffectUtil.handleChangedActives(provider, potential, previous)) {
            activeComponents = potential;
            sync();
        }
    }

    public void sync() {
        EffectAPI.getHelper().sendClientboundTracking(new SyncEffectsClientboundPacket(provider.getId(), allComponents.entrySet().stream().map(entry -> Pair.of(entry.getKey().id(), entry.getValue())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)), allComponents.keySet().stream().toList(), activeComponents), provider);
    }

    public void addEffect(EffectAPIEffect effect, EffectSource source) {
        Map<EffectSource, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new HashMap<>();

        for (Map.Entry<EffectSource, DataComponentMap> holder : allComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
        }
        newMap.computeIfAbsent(source, k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(effect.type(), t -> new ArrayList<>()).add(effect);

        Map<EffectSource, DataComponentMap> finalMap = new HashMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }
        allComponents = Map.copyOf(finalMap);
        combineComponents();
    }

    public void removeEffect(EffectAPIEffect effect, EffectSource source) {
        Map<EffectSource, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new HashMap<>();

        for (Map.Entry<EffectSource, DataComponentMap> holder : allComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    for (var value : list)
                        if (!holder.getKey().equals(source) || !value.equals(effect))
                            newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
        }

        Map<EffectSource, DataComponentMap> finalMap = new HashMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }
        allComponents = Map.copyOf(finalMap);
        combineComponents();
    }

    public void combineComponents() {
        Map<DataComponentType<?>, Set<EffectAPIEffect>> map = new HashMap<>();
        for (DataComponentMap componentMap : allComponents.values()) {
            for (var value : componentMap)
                map.computeIfAbsent(value.type(), t -> new HashSet<>()).addAll((Collection<? extends EffectAPIEffect>) value.value());
        }
        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var value : map.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) value.getKey(), ImmutableList.copyOf(value.getValue()));
        combinedComponents = builder.build();
    }

    @ApiStatus.Internal
    public void setComponents(Map<EffectSource, DataComponentMap> allComponents, DataComponentMap activeComponents) {
        removeNotPresentModdedComponents(allComponents);
        this.allComponents = allComponents;
        combineComponents();
        this.activeComponents = activeComponents;
    }

    private void removeNotPresentModdedComponents(Map<EffectSource, DataComponentMap> allComponents) {
        allComponents.keySet().removeIf(source -> {
            boolean bl = !EffectAPI.getHelper().isModLoaded(source.id().getNamespace());
            if (bl && EffectAPI.getHelper().isDevelopmentEnvironment())
                throw new IllegalArgumentException("Attempted to load an invalid modded Effect API effect into attachment. Make sure the namespace of the component (" + source.id().getNamespace() +") is of a loaded mod. This exception only happens inside developer environments, in production, the component will just not load.");
            return bl;
        });
    }

    public static Map<EffectSource, DataComponentMap> loadMap(Map<ResourceLocation, DataComponentMap> map, List<EffectSource> sources) {
        Map<EffectSource, DataComponentMap> newMap = new HashMap<>();
        for (var entry : map.entrySet()) {
            Optional<EffectSource> source = sources.stream().filter(effectSource -> effectSource.id() == entry.getKey()).findFirst();
            source.ifPresent(effectSource -> newMap.put(effectSource, entry.getValue()));
        }
        return newMap;
    }
}
