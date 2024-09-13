package house.greenhouse.effectapi.impl.attachment;

import com.google.common.collect.ImmutableList;
import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.impl.util.InternalEffectUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class EffectsAttachmentImpl<T> implements EffectsAttachment<T> {
    private final Map<EffectAPIEffect, LootContext> contexts = new HashMap<>();

    protected Object2ObjectArrayMap<ResourceLocation, DataComponentMap> sourcesToComponents = new Object2ObjectArrayMap<>();
    protected DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    protected DataComponentMap activeComponents = DataComponentMap.EMPTY;

    protected T provider;

    public void init(T provider) {
        if (this.provider != null)
            return;
        this.provider = provider;
        combineComponents();
        updateActiveComponents();
    }

    public abstract LootContext createLootContext(EffectAPIEffect effect, ResourceLocation source);

    public abstract void sync(ServerPlayer player);

    public abstract void sync();

    public abstract LootContextParamSet paramSet();

    public boolean isEmpty() {
        return sourcesToComponents.isEmpty();
    }

    @Override
    public <E extends EffectAPIEffect> List<E> getEffects(DataComponentType<List<E>> type, boolean includeInactive) {
        return includeInactive ? combinedComponents.getOrDefault(type, List.of()) : activeComponents.getOrDefault(type, List.of());
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffectType(DataComponentType<List<E>> type, boolean includeInactive) {
        return includeInactive ? combinedComponents.keySet().contains(type) : activeComponents.keySet().contains(type);
    }

    @Override
    public <E extends EffectAPIEffect> boolean isActive(E effect) {
        return hasEffect(effect, false);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive) {
        return includeInactive ? combinedComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)) : activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect));
    }

    public void tick() {
        updateActiveComponents();
        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            LootContext context = contexts.get(effect);
            if (effect.shouldTick(context, hasEffect(effect, false)))
                effect.tick(context);
        });
    }

    public void refresh() {
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect ->
                effect.onRefreshed(contexts.get(effect)));
    }

    public void addEffect(EffectAPIEffect effect, ResourceLocation source) {
        Object2ObjectArrayMap<ResourceLocation, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new Object2ObjectArrayMap<>();

        for (Map.Entry<ResourceLocation, DataComponentMap> holder : sourcesToComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
        }

        newMap.computeIfAbsent(source, k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(effect.type(), t -> new ArrayList<>()).add(effect);
        contexts.put(effect, createLootContext(effect, source));

        Object2ObjectArrayMap<ResourceLocation, DataComponentMap> finalMap = new Object2ObjectArrayMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }
        sourcesToComponents = finalMap;
        combineComponents();
    }

    public void removeEffect(EffectAPIEffect effect, ResourceLocation source) {
        if (sourcesToComponents.isEmpty())
            return;

        Object2ObjectArrayMap<ResourceLocation, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new Object2ObjectArrayMap<>();
        for (Map.Entry<ResourceLocation, DataComponentMap> holder : sourcesToComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect) {
                    List<EffectAPIEffect> effects = (List<EffectAPIEffect>) new ArrayList<>(list);
                    effects.remove(effect);
                    if (effects.isEmpty())
                        continue;
                    newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll(effects);
                }
        }

        Object2ObjectArrayMap<ResourceLocation, DataComponentMap> finalMap = new Object2ObjectArrayMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }

        effect.onRemoved(contexts.get(effect));
        sourcesToComponents = finalMap;
        contexts.remove(effect);
        combineComponents();
    }

    private void updateActiveComponents() {
        DataComponentMap previous = activeComponents;
        var newComponents = InternalEffectUtil.generateActiveEffectsIfNecessary(contexts, paramSet(), combinedComponents, previous);
        if (newComponents.isEmpty())
            return;
        activeComponents = newComponents.get();
        sync();
    }

    private void combineComponents() {
        Map<DataComponentType<?>, Set<EffectAPIEffect>> map = new HashMap<>();
        for (DataComponentMap componentMap : sourcesToComponents.values()) {
            for (var value : componentMap)
                map.computeIfAbsent(value.type(), t -> new HashSet<>()).addAll((Collection<? extends EffectAPIEffect>) value.value());
        }
        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var value : map.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) value.getKey(), ImmutableList.copyOf(value.getValue()));
        combinedComponents = builder.build();
    }

    public void setComponents(Object2ObjectArrayMap<ResourceLocation, DataComponentMap> allComponents, DataComponentMap activeComponents) {
        this.sourcesToComponents = allComponents;
        combineComponents();
        this.activeComponents = activeComponents;
    }
}