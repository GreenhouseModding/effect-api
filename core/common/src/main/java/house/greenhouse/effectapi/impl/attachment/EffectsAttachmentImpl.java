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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class EffectsAttachmentImpl<T> implements EffectsAttachment<T> {
    private final Map<EffectHolder<EffectAPIEffect>, EffectAPIEffect> effectLookup = new HashMap<>();
    private final Map<EffectAPIEffect, EffectHolder<EffectAPIEffect>> reverseEffectLookup = new HashMap<>();
    private final Map<EffectHolder<EffectAPIEffect>, LootContext> contexts = new HashMap<>();
    private final Map<EffectHolder<EffectAPIEffect>, Map<String, Object>> variableValues = new HashMap<>();

    protected Object2ObjectArrayMap<ResourceLocation, List<EffectHolder<EffectAPIEffect>>> variableHolderComponents = new Object2ObjectArrayMap<>();
    protected DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    protected DataComponentMap activeComponents = DataComponentMap.EMPTY;

    protected T provider;

    public void init(T provider) {
        this.provider = provider;
        combinedComponents = DataComponentMap.EMPTY;
        activeComponents = DataComponentMap.EMPTY;
        combineComponents();
        updateActiveComponents(Map.of());
    }

    public abstract int getTickCount();

    public abstract <E extends EffectAPIEffect> LootContext createLootContext(EffectHolder<E> variableHolder, ResourceLocation source);

    public abstract void sync(ServerPlayer player);

    public abstract void sync();

    public abstract LootContextParamSet paramSet();

    public boolean isEmpty() {
        return variableHolderComponents.isEmpty();
    }

    @Override
    public <E extends EffectAPIEffect> E getEffect(EffectHolder<E> holder) {
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
    public <E extends EffectAPIEffect> boolean hasEffect(EffectHolder<E> effect, boolean includeInactive) {
        return hasEffect(effectLookup.get(effect), includeInactive);
    }

    @Override
    public <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive) {
        return includeInactive ? combinedComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect)) : activeComponents.stream().anyMatch(typedDataComponent -> typedDataComponent.value() instanceof List<?> list && list.stream().anyMatch(o -> o == effect));
    }

    public void tick() {
        var holders = effectLookup.keySet().stream().filter(holder -> !holder.getPreviousValues(contexts.get(holder)).equals(this.variableValues.get(holder))).collect(Collectors.toMap(Function.identity(), this::getEffect));

        if (!holders.isEmpty()) {
            for (Map.Entry<EffectHolder<EffectAPIEffect>, EffectAPIEffect> effect : holders.entrySet())
                effect.getValue().onRemoved(contexts.get(effect.getKey()));
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

    public void addEffect(EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        variableHolderComponents.computeIfAbsent(source, s -> new ObjectArrayList<>()).add(effect);
        combineComponents();
        updateActiveComponents(Map.of());
        effectLookup.get(effect).onAdded(contexts.get(effect));
    }

    public void removeEffect(EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (variableHolderComponents.isEmpty())
            return;
        removeEffectInternal(effect, source);
        combineComponents();
        updateActiveComponents(Map.of());
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

    private void updateActiveComponents(Map<EffectHolder<EffectAPIEffect>, EffectAPIEffect> holders) {
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