package dev.greenhouseteam.effectapi.api.attachment;

import com.google.common.collect.ImmutableList;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.effect.EntityTickEffect;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncEffectsAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.util.InternalEffectUtil;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class EntityEffectsAttachment {
    public static final ResourceLocation ID = EffectAPI.asResource("entity_effects");

    private Map<ResourceLocation, DataComponentMap> allComponents = new HashMap<>();
    private DataComponentMap combinedComponents = DataComponentMap.EMPTY;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;
    private Entity provider;

    public EntityEffectsAttachment() {}

    public void init(Entity entity) {
        if (provider != null)
            return;
        this.provider = entity;
        updateActiveComponents(false);
        sync();
    }

    public boolean isEmpty() {
        return allComponents.isEmpty();
    }

    public <T> List<T> getEffects(DataComponentType<List<T>> type) {
        return activeComponents.getOrDefault(type, List.of());
    }

    public void tick() {
        updateActiveComponents(true);
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect -> {
            if (effect.type() == EffectAPIEffectTypes.ENTITY_TICK)
                InternalEffectUtil.<EntityTickEffect<?>>castConditional(effect).tick(InternalEffectUtil.createEntityOnlyContext(provider));
        });
    }

    public void refresh() {
        InternalEffectUtil.executeOnAllEffects(activeComponents, effect -> {
            effect.onRefreshed(InternalEffectUtil.createEntityOnlyContext(provider));
        });
    }

    private void updateActiveComponents(boolean sync) {
        DataComponentMap previous = activeComponents;
        DataComponentMap potential = InternalEffectUtil.generateActiveEffects(InternalEffectUtil.createEntityOnlyContext(provider), EffectAPILootContextParamSets.ENTITY, combinedComponents);
        if (InternalEffectUtil.hasUpdatedActives(provider, potential, previous)) {
            activeComponents = potential;
            if (sync)
                sync();
        }
    }

    public void sync() {
        EffectAPI.getHelper().sendClientboundTracking(new SyncEffectsAttachmentClientboundPacket(provider.getId(), allComponents, activeComponents), provider);
    }

    public void addEffect(EffectAPIEffect effect, ResourceLocation source) {
        Map<ResourceLocation, Map<DataComponentType<?>, List<EffectAPIEffect>>> newMap = new HashMap<>();

        for (Map.Entry<ResourceLocation, DataComponentMap> holder : allComponents.entrySet()) {
            for (var component : holder.getValue())
                if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    newMap.computeIfAbsent(holder.getKey(), k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(component.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
        }
        newMap.computeIfAbsent(source, k -> new Reference2ObjectArrayMap<>()).computeIfAbsent(effect.type(), t -> new ArrayList<>()).add(effect);

        Map<ResourceLocation, DataComponentMap> finalMap = new HashMap<>();
        for (var entry : newMap.entrySet()) {
            DataComponentMap.Builder builder = DataComponentMap.builder();
            for (var val : entry.getValue().entrySet())
                builder.set((DataComponentType<? super List<EffectAPIEffect>>) val.getKey(), List.copyOf(val.getValue()));
            finalMap.put(entry.getKey(), builder.build());
        }
        allComponents = Map.copyOf(finalMap);
        combineComponents();
        updateActiveComponents(false);
    }

    public void removeEffect(EffectAPIEffect effect, ResourceLocation source) {
        if (allComponents.isEmpty())
            return;
        Map<ResourceLocation, DataComponentMap> newMap = new HashMap<>(allComponents);

        newMap.get(source).keySet().removeIf(type -> ((List<?>)allComponents.get(source).getOrDefault(type, List.of())).stream().anyMatch(object -> object == effect));
        if (newMap.get(source).isEmpty())
            newMap.remove(source);

        allComponents = Map.copyOf(newMap);
        combineComponents();
        updateActiveComponents(false);
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
    public void setComponents(Map<ResourceLocation, DataComponentMap> allComponents, DataComponentMap activeComponents) {
        this.allComponents = allComponents;
        combineComponents();
        this.activeComponents = activeComponents;
    }
}