package house.greenhouse.effectapi.entity.api.attachment;

import com.google.common.collect.ImmutableList;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.effect.EntityTickEffect;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.util.InternalEffectUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Move to generic effect attachment class.
public class EntityEffectsAttachment {
    public static final ResourceLocation ID = EffectAPI.asResource("entity_effects");

    private Object2ObjectArrayMap<ResourceLocation, DataComponentMap> allComponents = new Object2ObjectArrayMap<>();
    private DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;
    private Entity provider;
    private final Map<EffectAPIEffect, LootContext> contexts = new HashMap<>();

    public EntityEffectsAttachment() {}

    public void init(Entity entity) {
        if (provider != null)
            return;
        this.provider = entity;
        updateActiveComponents(true);
    }

    public boolean isEmpty() {
        return allComponents.isEmpty();
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
        updateActiveComponents(true);
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

    private void updateActiveComponents(boolean sync) {
        DataComponentMap previous = activeComponents;
        if (!InternalEffectUtil.haveActivesChanged(contexts, EffectAPIEntityLootContextParamSets.ENTITY, combinedComponents, previous)) {
            InternalEffectUtil.clearChangedCache();
            return;
        }
        activeComponents = InternalEffectUtil.generateActiveEffects(contexts, EffectAPIEntityLootContextParamSets.ENTITY, combinedComponents, previous);
        if (sync)
            sync();
    }

    public void sync() {
        EffectAPI.getHelper().sendClientboundTracking(new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), allComponents, activeComponents), provider);
    }

    public void addEffect(EffectAPIEffect effect, ResourceLocation source) {
        Object2ObjectArrayMap<ResourceLocation, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new Object2ObjectArrayMap<>();

        for (Map.Entry<ResourceLocation, DataComponentMap> holder : allComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
        }

        newMap.computeIfAbsent(source, k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(effect.type(), t -> new ArrayList<>()).add(effect);
        contexts.put(effect, EntityEffectAPI.createEntityOnlyContext(provider, source));

        Object2ObjectArrayMap<ResourceLocation, DataComponentMap> finalMap = new Object2ObjectArrayMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }
        allComponents = finalMap;
        combineComponents();
        updateActiveComponents(true);
    }

    public void removeEffect(EffectAPIEffect effect, ResourceLocation source) {
        if (allComponents.isEmpty())
            return;
        Object2ObjectArrayMap<ResourceLocation, DataComponentMap> newMap = new Object2ObjectArrayMap<>(allComponents);

        newMap.get(source).keySet().removeIf(type -> ((List<?>)allComponents.get(source).getOrDefault(type, List.of())).stream().anyMatch(object -> object == effect));
        if (newMap.get(source).isEmpty())
            newMap.remove(source);

        allComponents = newMap;
        combineComponents();
        updateActiveComponents(true);
        contexts.remove(effect);
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
    public void setComponents(Object2ObjectArrayMap<ResourceLocation, DataComponentMap> allComponents, DataComponentMap activeComponents) {
        this.allComponents = allComponents;
        combineComponents();
        this.activeComponents = activeComponents;
    }
}