package house.greenhouse.effectapi.entity.api.attachment;

import com.google.common.collect.ImmutableList;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.EntityEffectUtil;
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
    private final Map<EffectAPIEffect, ResourceLocation> sources = new HashMap<>();
    private DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;
    private Entity provider;

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

    public <T> List<T> getEffects(DataComponentType<List<T>> type) {
        return activeComponents.getOrDefault(type, List.of());
    }

    public <T> boolean hasEffectType(DataComponentType<List<T>> type) {
        return activeComponents.keySet().contains(type);
    }

    public void tick() {
        updateActiveComponents(true);
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect -> {
            if (effect.type() == EffectAPIEntityEffectTypes.ENTITY_TICK)
                InternalEffectUtil.<EntityTickEffect<?>>castConditional(effect).tick(EntityEffectUtil.createEntityOnlyContext(provider, sources.getOrDefault(effect, null)));
        });
    }

    public void refresh() {
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect ->
                effect.onRefreshed(EntityEffectUtil.createEntityOnlyContext(provider, sources.getOrDefault(effect, null))));
    }

    private void updateActiveComponents(boolean sync) {
        DataComponentMap previous = activeComponents;
        if (!InternalEffectUtil.haveActivesChanged(EntityEffectUtil.createEntityOnlyContext(provider), EffectAPIEntityLootContextParamSets.ENTITY, combinedComponents, previous, sources)) {
            InternalEffectUtil.clearChangedCache();
            return;
        }
        activeComponents = InternalEffectUtil.generateActiveEffects(EntityEffectUtil.createEntityOnlyContext(provider), EffectAPIEntityLootContextParamSets.ENTITY, combinedComponents, previous, sources);
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
        addToSources(effect, source);

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
        sources.remove(effect);
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

    private void addToSources(EffectAPIEffect effect, ResourceLocation source) {
        if (sources.containsKey(effect))
            return;
        sources.put(effect, source);
    }

    @ApiStatus.Internal
    public void setComponents(Object2ObjectArrayMap<ResourceLocation, DataComponentMap> allComponents, DataComponentMap activeComponents) {
        this.allComponents = allComponents;
        combineComponents();
        this.activeComponents = activeComponents;
    }
}