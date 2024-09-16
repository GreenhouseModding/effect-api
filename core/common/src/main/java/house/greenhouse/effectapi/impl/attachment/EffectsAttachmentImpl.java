package house.greenhouse.effectapi.impl.attachment;

import com.google.common.collect.ImmutableList;
import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.impl.util.InternalEffectUtil;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class EffectsAttachmentImpl<T> implements EffectsAttachment<T> {
    private final Map<VariableHolder<EffectAPIEffect>, EffectAPIEffect> effectLookup = new HashMap<>();
    private final Map<EffectAPIEffect, VariableHolder<EffectAPIEffect>> reverseEffectLookup = new HashMap<>();
    private final Map<VariableHolder<EffectAPIEffect>, LootContext> contexts = new HashMap<>();
    private final Map<VariableHolder<EffectAPIEffect>, Map<String, Object>> variableValues = new HashMap<>();

    protected Object2ObjectArrayMap<ResourceLocation, List<VariableHolder<EffectAPIEffect>>> variableHolderComponents = new Object2ObjectArrayMap<>();
    protected DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    protected DataComponentMap activeComponents = DataComponentMap.EMPTY;

    protected T provider;

    public void init(T provider) {
        if (this.provider != null)
            return;
        this.provider = provider;
        combineComponents();
        updateActiveComponents(Set.of());
    }

    public abstract int getTickCount();

    public abstract LootContext createLootContext(ResourceLocation source);

    public abstract void sync(ServerPlayer player);

    public abstract void sync();

    public abstract LootContextParamSet paramSet();

    public boolean isEmpty() {
        return variableHolderComponents.isEmpty();
    }

    @Override
    public <E extends EffectAPIEffect> E getEffect(VariableHolder<E> holder) {
        return (E) effectLookup.get(holder);
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
    public <E extends EffectAPIEffect> boolean hasEffect(VariableHolder<E> effect, boolean includeInactive) {
        return hasEffect(effectLookup.get(effect), includeInactive);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive) {
        return includeInactive ? combinedComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)) : activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect));
    }

    public void tick() {
        var holders = effectLookup.keySet().stream().filter(holder -> !holder.getPreviousValues(contexts.get(holder)).equals(this.variableValues.get(holder))).collect(Collectors.toSet());

        if (!holders.isEmpty()) {
            for (VariableHolder<EffectAPIEffect> holder : holders)
                effectLookup.get(holder).onRemoved(contexts.get(holder));
            combineComponents();
        }

        updateActiveComponents(holders);
        InternalEffectUtil.executeOnAllEffects(combinedComponents, effect -> {
            LootContext context = contexts.get(reverseEffectLookup.get(effect));
            if (effect.shouldTick(context, hasEffect(effect, false), getTickCount()))
                effect.tick(context, getTickCount());
        });
    }

    public void refresh() {
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect ->
                effect.onRefreshed(contexts.get(reverseEffectLookup.get(effect))));
    }

    public void addEffect(VariableHolder<EffectAPIEffect> effect, ResourceLocation source) {
        variableHolderComponents.computeIfAbsent(source, s -> new ObjectArrayList<>()).add(effect);
        combineComponents();
        updateActiveComponents(Set.of());
    }

    public void removeEffect(VariableHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (variableHolderComponents.isEmpty())
            return;
        removeEffectInternal(effect, source);
        combineComponents();
        updateActiveComponents(Set.of());
    }

    private void removeEffectInternal(VariableHolder<EffectAPIEffect> effect, ResourceLocation source) {
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

    private void updateActiveComponents(Set<VariableHolder<EffectAPIEffect>> holders) {
        DataComponentMap previous = activeComponents;
        var newComponents = InternalEffectUtil.generateActiveEffectsIfNecessary(contexts, holders, reverseEffectLookup, paramSet(), combinedComponents, previous, getTickCount());
        if (newComponents.isEmpty())
            return;
        activeComponents = newComponents.get();
        sync();
    }

    private void combineComponents() {
        effectLookup.clear();
        reverseEffectLookup.clear();
        Map<DataComponentType<?>, Set<EffectAPIEffect>> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, List<VariableHolder<EffectAPIEffect>>> componentMap : variableHolderComponents.entrySet()) {
            for (var value : componentMap.getValue()) {
                LootContext context = createLootContext(componentMap.getKey());
                variableValues.put(value, value.getPreviousValues(context));
                EffectAPIEffect effect = value.construct(context, variableValues.get(value));
                if (effect == null) {
                    removeEffectInternal(value, componentMap.getKey());
                    return;
                }
                contexts.put(value, context);
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