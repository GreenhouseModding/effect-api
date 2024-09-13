package house.greenhouse.effectapi.api.attachment;

import com.google.common.collect.ImmutableList;
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
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Make this a split interface and class.
public class EffectsAttachment<T> {
    private final Map<EffectAPIEffect, LootContext> contexts = new HashMap<>();
    private final ContextFunction<T> contextFunction;
    private final IndividualNetworkFunction<T> individualNetworkFunction;
    
    private final TrackingNetworkFunction<T> trackingNetworkFunction;
    private final LootContextParamSet paramSet;

    private Object2ObjectArrayMap<ResourceLocation, DataComponentMap> sourcesToComponents = new Object2ObjectArrayMap<>();
    private DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;

    private T provider;

    public EffectsAttachment(ContextFunction<T> contextFunction, IndividualNetworkFunction<T> individualNetworkFunction, TrackingNetworkFunction<T> trackingNetworkFunction, LootContextParamSet paramSet) {
        this.contextFunction = contextFunction;
        this.individualNetworkFunction = individualNetworkFunction;
        this.trackingNetworkFunction = trackingNetworkFunction;
        this.paramSet = paramSet;
    }

    public void init(T provider) {
        if (this.provider != null)
            return;
        this.provider = provider;
        updateActiveComponents();
    }

    public boolean isEmpty() {
        return sourcesToComponents.isEmpty();
    }

    public <T extends EffectAPIEffect> List<T> getEffects(DataComponentType<List<T>> type, boolean includeInactive) {
        return includeInactive ? combinedComponents.getOrDefault(type, List.of()) : activeComponents.getOrDefault(type, List.of());
    }

    public <T extends EffectAPIEffect> boolean isTypeActive(DataComponentType<List<T>> type) {
        return hasEffectType(type, false);
    }

    public <T extends EffectAPIEffect> boolean hasEffectType(DataComponentType<List<T>> type, boolean includeInactive) {
        return includeInactive ? combinedComponents.keySet().contains(type) : activeComponents.keySet().contains(type);
    }

    public <T extends EffectAPIEffect> boolean isActive(T effect) {
        return hasEffect(effect, false);
    }

    public <T extends EffectAPIEffect> boolean hasEffect(T effect, boolean includeInactive) {
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

    private void updateActiveComponents() {
        DataComponentMap previous = activeComponents;
        var newComponents = InternalEffectUtil.generateActiveEffectsIfNecessary(contexts, paramSet, combinedComponents, previous);
        if (newComponents.isEmpty())
            return;
        activeComponents = newComponents.get();
        syncToAll();
    }

    public void syncToPlayer(ServerPlayer player) {
        individualNetworkFunction.send(provider, sourcesToComponents, activeComponents, player);
    }

    public void syncToAll() {
        trackingNetworkFunction.send(provider, sourcesToComponents, activeComponents);
    }

    public void addEffect(EffectAPIEffect effect, ResourceLocation source) {
        Object2ObjectArrayMap<ResourceLocation, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new Object2ObjectArrayMap<>();

        for (Map.Entry<ResourceLocation, DataComponentMap> holder : sourcesToComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
        }

        newMap.computeIfAbsent(source, k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(effect.type(), t -> new ArrayList<>()).add(effect);
        contexts.put(effect, contextFunction.apply(provider, effect, source));

        Object2ObjectArrayMap<ResourceLocation, DataComponentMap> finalMap = new Object2ObjectArrayMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }
        sourcesToComponents = finalMap;
        combineComponents();
        updateActiveComponents();
    }

    public void removeEffect(EffectAPIEffect effect, ResourceLocation source) {
        if (sourcesToComponents.isEmpty())
            return;
        Object2ObjectArrayMap<ResourceLocation, DataComponentMap> newMap = new Object2ObjectArrayMap<>(sourcesToComponents);

        newMap.get(source).keySet().removeIf(type -> ((List<?>)sourcesToComponents.get(source).getOrDefault(type, List.of())).stream().anyMatch(object -> object == effect));
        if (newMap.get(source).isEmpty())
            newMap.remove(source);

        sourcesToComponents = newMap;
        combineComponents();
        updateActiveComponents();
        contexts.remove(effect);
    }

    public void combineComponents() {
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

    @ApiStatus.Internal
    public void setComponents(Object2ObjectArrayMap<ResourceLocation, DataComponentMap> allComponents, DataComponentMap activeComponents) {
        this.sourcesToComponents = allComponents;
        combineComponents();
        this.activeComponents = activeComponents;
    }

    @FunctionalInterface
    public interface ContextFunction<T> {
        LootContext apply(T provider, EffectAPIEffect effect, ResourceLocation source);
    }

    @FunctionalInterface
    public interface IndividualNetworkFunction<T> {
        void send(T provider, Object2ObjectArrayMap<ResourceLocation, DataComponentMap> sourcesToComponents, DataComponentMap activeComponents, ServerPlayer receiver);
    }
    
    @FunctionalInterface
    public interface TrackingNetworkFunction<T> {
        void send(T provider, Object2ObjectArrayMap<ResourceLocation, DataComponentMap> sourcesToComponents, DataComponentMap activeComponents);
    }
}