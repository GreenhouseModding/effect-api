package dev.greenhouseteam.effectapi.api.util;

import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EffectUtil {
    public static DataComponentMap getActive(Entity entity, DataComponentMap map) {
        if (entity == null || map == DataComponentMap.EMPTY)
            return DataComponentMap.EMPTY;

        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();

        for (TypedDataComponent<?> component : map) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>)list)) {
                    if (effect.paramSet() == EffectAPILootContextParamSets.ENTITY && effect.isActive(EffectAPIEffect.createEntityOnlyContext(entity))) {
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                    }
                }
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet()) {
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static boolean hasUpdatedActives(Entity entity, DataComponentMap newMap, DataComponentMap oldMap) {
        List<?> oldValues = oldMap.stream().map(TypedDataComponent::value).toList();
        List<?> newValues = newMap.stream().map(TypedDataComponent::value).toList();

        if (oldValues.equals(newValues))
            return false;

        newValues.stream().filter(object -> !oldValues.contains(object)).forEach(value -> {
            if (value instanceof List<?> list)
                for (Object v : list)
                    if (v instanceof EffectAPIEffect effect)
                        if (effect.paramSet() == EffectAPILootContextParamSets.ENTITY)
                            effect.onAdded(EffectAPIEffect.createEntityOnlyContext(entity));
        });
        oldValues.stream().filter(object -> !newValues.contains(object)).forEach(value -> {
            if (value instanceof List<?> list)
                for (Object v : list)
                    if (v instanceof EffectAPIEffect effect)
                        if (effect.paramSet() == EffectAPILootContextParamSets.ENTITY)
                            effect.onRemoved(EffectAPIEffect.createEntityOnlyContext(entity));
        });

        return true;
    }
}
