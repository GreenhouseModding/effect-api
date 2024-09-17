package house.greenhouse.effectapi.impl.attachment;

import com.google.common.collect.ImmutableList;
import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.impl.util.InternalEffectUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public abstract class EffectsAttachmentImpl<T> implements EffectsAttachment<T> {
    private final Map<EffectHolder<EffectAPIEffect, T>, EffectAPIEffect> effectLookup = new HashMap<>();
    private final Map<EffectAPIEffect, EffectHolder<EffectAPIEffect, T>> reverseEffectLookup = new HashMap<>();
    private final Map<EffectHolder<EffectAPIEffect, T>, Map<String, Object>> variableValues = new HashMap<>();
    private final Map<EffectHolder<EffectAPIEffect, T>, Boolean> activeValues = new HashMap<>();

    protected Object2ObjectArrayMap<EffectHolder<EffectAPIEffect, T>, List<ResourceLocation>> sources = new Object2ObjectArrayMap<>();
    protected DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    protected DataComponentMap activeComponents = DataComponentMap.EMPTY;

    protected T provider;
    private boolean shouldSync;

    public void init(T provider) {
        this.provider = provider;
        variableValues.clear();
        combinedComponents = DataComponentMap.EMPTY;
        activeComponents = DataComponentMap.EMPTY;
        combineComponents();
    }

    public abstract int getTickCount();

    public void sync(ServerPlayer player) {
        syncInternal(player);
    }

    public void sync() {
        shouldSync = true;
    }

    protected abstract void syncInternal(ServerPlayer player);

    protected abstract void syncInternal();

    public abstract LootContextParamSet paramSet();

    public boolean isEmpty() {
        return sources.isEmpty();
    }

    @Override
    public <E extends EffectAPIEffect> E getEffect(EffectHolder<E, T> holder) {
        refreshEffect(holder);
        return (E) effectLookup.get(holder);
    }

    @Override
    public <E extends EffectAPIEffect> List<E> getEffects(EffectType<E, T> type, boolean includeInactive) {
        refreshMultipleEffects(effect -> effect.type() == type);
        return includeInactive ? (List<E>) combinedComponents.getOrDefault(type, List.of()) : (List<E>) activeComponents.getOrDefault(type, List.of());
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffectType(EffectType<E, T> type, boolean includeInactive) {
        refreshMultipleEffects(effect -> effect.type() == type);
        return includeInactive ? combinedComponents.keySet().contains(type) : activeComponents.keySet().contains(type);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(EffectHolder<E, T> holder, boolean includeInactive) {
        return hasEffect(effectLookup.get(holder), includeInactive);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive) {
        return includeInactive ? combinedComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)) : activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect));
    }

    public void tick() {
        List<EffectAPIEffect> passedEffects = new ArrayList<>();
        AtomicBoolean hasActiveChanged = new AtomicBoolean(false);
        Map<EffectHolder<EffectAPIEffect, T>, EffectAPIEffect> changedEffects = new HashMap<>();
        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            EffectHolder<EffectAPIEffect, T> holder = reverseEffectLookup.get(effect);
            LootContext context = ((EffectType)effect.type()).createContext(provider, sources.get(holder).getFirst());
            boolean isActive = effect.isActive(context, getTickCount());
            if (effect.shouldTick(context, isActive, getTickCount())) {
                if (!variableValues.containsKey(holder) || !holder.getPreviousValues(context).equals(variableValues.get(holder)))
                    changedEffects.put(holder, effect);
                else if (!activeValues.containsKey(holder) || activeValues.get(holder) != isActive) {
                    activeValues.put(holder, isActive);
                    hasActiveChanged.set(true);
                } else
                    passedEffects.add(effect);
            }
        });

        if (hasActiveChanged.get() || !changedEffects.isEmpty()) {
            combineComponents(changedEffects.keySet());
            updateActiveComponents(changedEffects);
            sync();
        }

        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            EffectHolder<EffectAPIEffect, T> holder = reverseEffectLookup.get(effect);
            LootContext context = ((EffectType)effect.type()).createContext(provider, sources.get(holder).getFirst());
            if (passedEffects.contains(effect) || effect.shouldTick(context, activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)), getTickCount()))
                effect.tick(context, getTickCount());
        });

        if (shouldSync) {
            syncInternal();
            shouldSync = false;
        }
    }

    public void refresh() {
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect -> {
            EffectHolder<EffectAPIEffect, T> holder = reverseEffectLookup.get(effect);
            effect.onRefreshed(((EffectType)effect.type()).createContext(provider, sources.get(holder).getFirst()));
        });
    }

    public void addEffect(EffectHolder<EffectAPIEffect, T> effect, ResourceLocation source) {
        sources.computeIfAbsent(effect, s -> new ObjectArrayList<>()).add(source);
        combineComponents();
        updateActiveComponents();
        effectLookup.get(effect).onAdded(effect.effectType().createContext(provider, sources.get(effect).getFirst()));
        sync();
    }

    public void removeEffect(EffectHolder<EffectAPIEffect, T> effect, ResourceLocation source) {
        if (sources.isEmpty())
            return;
        removeEffectInternal(effect, Set.of(source));
        combineComponents();
        updateActiveComponents();
        if (sources.isEmpty())
            syncInternal();
        else
            sync();
    }

    private void removeEffectInternal(EffectHolder<EffectAPIEffect, T> effect, Set<ResourceLocation> removals) {
        if (effectLookup.containsKey(effect)) {
            EffectAPIEffect inner = effectLookup.get(effect);
            inner.onRemoved(effect.effectType().createContext(provider, sources.get(effect).getFirst()));
            reverseEffectLookup.remove(inner);
        }
        effectLookup.remove(effect);
        sources.get(effect).removeIf(removals::contains);
        if (sources.get(effect).isEmpty())
            sources.remove(effect);
    }

    private void updateActiveComponents() {
        updateActiveComponents(null);
    }

    private <E extends EffectAPIEffect> void updateActiveComponents(@Nullable Map<EffectHolder<E, T>, E> previousHolders) {
        DataComponentMap previous = activeComponents;
        var newComponents = InternalEffectUtil.generateActiveEffectsIfNecessary(holder -> holder.effectType().createContext(provider, sources.get(holder).getFirst()), previousHolders, reverseEffectLookup, combinedComponents, previous, getTickCount());
        if (newComponents.isEmpty())
            return;
        activeComponents = newComponents.get();
    }

    private <E extends EffectAPIEffect> void refreshEffect(EffectHolder<E, T> holder) {
        if (!sources.containsKey(holder))
            return;
        LootContext context = holder.effectType().createContext(provider, sources.get(holder).getFirst());
        if ((!variableValues.containsKey(holder) || !holder.getPreviousValues(context).equals(variableValues.get(holder)))) {
            combineComponents(Set.of((EffectHolder<EffectAPIEffect, T>) holder));
            updateActiveComponents(Map.of(holder, (E)effectLookup.get(holder)));
            sync();
        } else {
            boolean isActive = effectLookup.get(holder).isActive(context, getTickCount());
            if (activeValues.get(holder) != isActive) {
                activeValues.put((EffectHolder) holder, isActive);
                combineComponents();
                updateActiveComponents();
                sync();
            }
        }
    }

    private void refreshMultipleEffects(Predicate<EffectAPIEffect> effectPredicate) {
        AtomicBoolean hasActiveChanged = new AtomicBoolean(false);
        Map<EffectHolder<EffectAPIEffect, T>, EffectAPIEffect> changedEffects = new HashMap<>();
        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            if (!reverseEffectLookup.containsKey(effect))
                return;
            EffectHolder<EffectAPIEffect, T> holder = reverseEffectLookup.get(effect);
            LootContext context = holder.effectType().createContext(provider, sources.get(holder).getFirst());
            if (effectPredicate.test(effect)) {
                if (!variableValues.containsKey(holder) || !holder.getPreviousValues(context).equals(variableValues.get(holder)))
                    changedEffects.put(holder, effect);
                else {
                    boolean isActive = effect.isActive(context, getTickCount());
                    if (!activeValues.containsKey(holder) || activeValues.get(holder) != isActive) {
                        activeValues.put(holder, isActive);
                        hasActiveChanged.set(true);
                    }
                }
            }
        });
        if (hasActiveChanged.get() || !changedEffects.isEmpty()) {
            combineComponents(changedEffects.keySet());
            updateActiveComponents(changedEffects);
            sync();
        }
    }

    private void combineComponents() {
        combineComponents(Set.of());
    }

    private void combineComponents(Set<EffectHolder<EffectAPIEffect, T>> variableChangedEffects) {
        Map<EffectType<?, T>, Set<EffectAPIEffect>> map = new HashMap<>();
        for (Map.Entry<EffectHolder<EffectAPIEffect, T>, List<ResourceLocation>> componentMap : sources.entrySet()) {
            EffectHolder<EffectAPIEffect, T> holder = componentMap.getKey();
            if (!effectLookup.containsKey(holder) || variableChangedEffects.contains(componentMap.getKey())) {
                LootContext context = holder.effectType().createContext(provider, sources.get(holder).getFirst());
                variableValues.put(holder, holder.getPreviousValues(context));
                EffectAPIEffect effect = holder.construct(context, variableValues.get(holder));
                if (effect == null) {
                    removeEffectInternal(holder, new HashSet<>(componentMap.getValue()));
                    return;
                }
                effectLookup.put(holder, effect);
                reverseEffectLookup.put(effect, holder);
            }
            map.computeIfAbsent(holder.effectType(), t -> new HashSet<>()).add(effectLookup.get(holder));
        }
        effectLookup.entrySet().removeIf(entry -> !map.containsKey(entry.getKey().effectType()) || !map.get(entry.getKey().effectType()).contains(entry.getValue()));
        reverseEffectLookup.values().removeIf(effect -> !effectLookup.containsKey(effect));

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var value : map.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) value.getKey(), ImmutableList.copyOf(value.getValue()));

        combinedComponents = builder.build();
    }

    public void setComponents(DataComponentMap combinedComponents, DataComponentMap activeComponents) {
        this.combinedComponents = combinedComponents;
        this.activeComponents = activeComponents;
    }
}