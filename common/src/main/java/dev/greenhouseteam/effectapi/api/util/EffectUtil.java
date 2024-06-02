package dev.greenhouseteam.effectapi.api.util;

import dev.greenhouseteam.effectapi.api.effect.Effect;
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

        Map<DataComponentType<?>, List<Effect>> newMap = new Reference2ObjectArrayMap<>();

        for (TypedDataComponent<?> component : map) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof Effect)
                for (Effect effect : ((List<Effect>)list)) {
                    if (effect.isActive(entity)) {
                        if (!effect.isContainer())
                            newMap.computeIfAbsent(effect.type(), type -> new ArrayList<>()).add(effect);
                    }
                }
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet()) {
            builder.set((DataComponentType<? super List<Effect>>) entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static List<Effect> getEffectsWithInner(Object effects) {
        if (!(effects instanceof List<?> list))
            return List.of();

        List<Effect> newList = new ArrayList<>(list.stream().map(object -> (Effect)object).toList());
        for (Effect effect : list.stream().map(object -> (Effect)object).toList())
            newList.addAll(effect.childrenAsEffects());

        return newList;
    }

    public static boolean handleChangedActives(Entity entity, DataComponentMap newMap, DataComponentMap oldMap) {
        List<TypedDataComponent<?>> oldValues = oldMap.stream().toList();
        List<TypedDataComponent<?>> newValues = newMap.stream().toList();

        if (oldValues.equals(newValues))
            return false;

        newValues.stream().filter(object -> !oldValues.contains(object)).forEach(component -> {
            if (component.value() instanceof List<?> list)
                for (Object value : list)
                    if (value instanceof Effect effect)
                        effect.onAdded(entity);
        });
        oldValues.stream().filter(object -> !newValues.contains(object)).forEach(component -> {
            if (component.value() instanceof List<?> list)
                for (Object value : list)
                    if (value instanceof Effect effect)
                        effect.onRemoved(entity);
        });

        return true;
    }
}
