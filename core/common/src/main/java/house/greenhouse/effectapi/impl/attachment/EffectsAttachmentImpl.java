package house.greenhouse.effectapi.impl.attachment;

import com.google.common.collect.ImmutableList;
import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.EffectHolder;
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
import java.util.function.Predicate;

public abstract class EffectsAttachmentImpl<T> implements EffectsAttachment<T> {
    private final Map<EffectHolder<EffectAPIEffect>, EffectAPIEffect> effectLookup = new HashMap<>();
    private final Map<EffectAPIEffect, EffectHolder<EffectAPIEffect>> reverseEffectLookup = new HashMap<>();
    private final Map<EffectHolder<EffectAPIEffect>, LootContext> contexts = new HashMap<>();
    private final Map<EffectHolder<EffectAPIEffect>, Map<String, Object>> variableValues = new HashMap<>();

    protected Object2ObjectArrayMap<ResourceLocation, List<EffectHolder<EffectAPIEffect>>> variableHolderComponents = new Object2ObjectArrayMap<>();
    protected DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    protected DataComponentMap activeComponents = DataComponentMap.EMPTY;

    protected T provider;
    private boolean shouldSync;

    public void init(T provider) {
        this.provider = provider;
        contexts.clear();
        variableValues.clear();
        combinedComponents = DataComponentMap.EMPTY;
        activeComponents = DataComponentMap.EMPTY;
        combineComponents();
    }

    public abstract int getTickCount();

    public abstract <E extends EffectAPIEffect> LootContext createLootContext(EffectHolder<E> variableHolder, ResourceLocation source);

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
        return variableHolderComponents.isEmpty();
    }

    @Override
    public <E extends EffectAPIEffect> E getEffect(EffectHolder<E> holder) {
        refreshEffect(holder);
        return (E) effectLookup.get(holder);
    }

    @Override
    public <E extends EffectAPIEffect> List<E> getEffects(DataComponentType<E> type, boolean includeInactive) {
        refreshMultipleEffects(effect -> effect.type() == type);
        return includeInactive ? (List<E>) combinedComponents.getOrDefault(type, List.of()) : (List<E>) activeComponents.getOrDefault(type, List.of());
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffectType(DataComponentType<E> type, boolean includeInactive) {
        refreshMultipleEffects(effect -> effect.type() == type);
        return includeInactive ? combinedComponents.keySet().contains(type) : activeComponents.keySet().contains(type);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(EffectHolder<E> holder, boolean includeInactive) {
        return hasEffect(effectLookup.get(holder), includeInactive);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive) {
        return includeInactive ? combinedComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)) : activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect));
    }

    public void tick() {
        List<EffectAPIEffect> passedEffects = new ArrayList<>();
        Map<EffectHolder<EffectAPIEffect>, EffectAPIEffect> changedEffects = new HashMap<>();
        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            EffectHolder<EffectAPIEffect> holder = reverseEffectLookup.get(effect);
            LootContext context = contexts.get(holder);
            if (effect.shouldTick(context, effect.isActive(context, getTickCount()), getTickCount())) {
                if (!variableValues.containsKey(holder) || !holder.getPreviousValues(context).equals(variableValues.get(holder)))
                    changedEffects.put(holder, effect);
                else {
                    passedEffects.add(effect);
                }
            }
        });

        if (!changedEffects.isEmpty()) {
            combineComponents();
            updateActiveComponents(changedEffects);
            sync();
        }

        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            EffectHolder<EffectAPIEffect> holder = reverseEffectLookup.get(effect);
            LootContext context = contexts.get(holder);
            if (passedEffects.contains(effect) || effect.shouldTick(context, activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)), getTickCount()))
                effect.tick(context, getTickCount());
        });

        if (shouldSync) {
            syncInternal();
            shouldSync = false;
        }
    }

    public void refresh() {
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect ->
                effect.onRefreshed(contexts.get(reverseEffectLookup.get(effect))));
    }

    public void addEffect(EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        variableHolderComponents.computeIfAbsent(source, s -> new ObjectArrayList<>()).add(effect);
        combineComponents();
        updateActiveComponents();
        effectLookup.get(effect).onAdded(contexts.get(effect));
        sync();
    }

    public void removeEffect(EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (variableHolderComponents.isEmpty())
            return;
        removeEffectInternal(effect, source);
        combineComponents();
        updateActiveComponents();
        if (variableHolderComponents.isEmpty())
            syncInternal();
        else
            sync();
    }

    private void removeEffectInternal(EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (effectLookup.containsKey(effect)) {
            EffectAPIEffect inner = effectLookup.get(effect);
            inner.onRemoved(contexts.get(effect));
            reverseEffectLookup.remove(inner);
        }
        contexts.remove(effect);
        effectLookup.remove(effect);
        variableHolderComponents.get(source).remove(effect);
        if (variableHolderComponents.get(source).isEmpty())
            variableHolderComponents.remove(source);
    }

    private void updateActiveComponents() {
        updateActiveComponents(null);
    }

    private <E extends EffectAPIEffect> void updateActiveComponents(@Nullable Map<EffectHolder<E>, E> previousHolders) {
        DataComponentMap previous = activeComponents;
        var newComponents = InternalEffectUtil.generateActiveEffectsIfNecessary(contexts, previousHolders, reverseEffectLookup, combinedComponents, previous, getTickCount());
        if (newComponents.isEmpty())
            return;
        activeComponents = newComponents.get();
    }

    private <E extends EffectAPIEffect> void refreshEffect(EffectHolder<E> holder) {
        if (contexts.containsKey(holder) && (!variableValues.containsKey(holder) || !holder.getPreviousValues(contexts.get(holder)).equals(variableValues.get(holder)))) {
            combineComponents();
            updateActiveComponents(Map.of(holder, (E)effectLookup.get(holder)));
            sync();
        }
    }

    private void refreshMultipleEffects(Predicate<EffectAPIEffect> effectPredicate) {
        Map<EffectHolder<EffectAPIEffect>, EffectAPIEffect> changedEffects = new HashMap<>();
        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            if (!reverseEffectLookup.containsKey(effect))
                return;
            EffectHolder<EffectAPIEffect> holder = reverseEffectLookup.get(effect);
            LootContext context = contexts.get(holder);
            if (effectPredicate.test(effect) && (!variableValues.containsKey(holder) || !holder.getPreviousValues(context).equals(variableValues.get(holder))))
                changedEffects.put(holder, effect);
        });
        if (!changedEffects.isEmpty()) {
            combineComponents();
            updateActiveComponents(changedEffects);
            sync();
        }
    }

    private void combineComponents() {
        effectLookup.clear();
        reverseEffectLookup.clear();
        Map<DataComponentType<?>, Set<EffectAPIEffect>> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, List<EffectHolder<EffectAPIEffect>>> componentMap : variableHolderComponents.entrySet()) {
            for (var value : componentMap.getValue()) {
                if (!contexts.containsKey(value))
                    contexts.put(value, createLootContext(value, componentMap.getKey()));
                variableValues.put(value, value.getPreviousValues(contexts.get(value)));
                EffectAPIEffect effect = value.construct(contexts.get(value), variableValues.get(value));
                if (effect == null) {
                    removeEffectInternal(value, componentMap.getKey());
                    return;
                }
                contexts.put(value, contexts.get(value));
                effectLookup.put(value, effect);
                reverseEffectLookup.put(effect, value);
                map.computeIfAbsent(effect.type(), t -> new HashSet<>()).add(effect);
            }
        }
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