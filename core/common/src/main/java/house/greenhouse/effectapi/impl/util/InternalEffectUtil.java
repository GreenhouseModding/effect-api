package house.greenhouse.effectapi.impl.util;

import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InternalEffectUtil {
    public static void executeOnAllEffects(DataComponentMap map, Consumer<EffectAPIEffect> consumer) {
        for (var entry : map)
            if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> consumer.accept((EffectAPIEffect)effect));
    }

    public static <T extends EffectAPIEffect> T castConditional(EffectAPIEffect effect) {
        return (T) ((EffectAPIConditionalEffect)effect).effect();
    }

    private static final List<EffectAPIEffect> UNCHANGED = new ArrayList<>();
    private static EffectAPIEffect added;
    private static EffectAPIEffect removed;

    public static void clearChangedCache() {
        UNCHANGED.clear();
        added = null;
        removed = null;
    }

    public static boolean haveActivesChanged(Map<EffectAPIEffect, LootContext> contexts, LootContextParamSet paramSet,
                                             DataComponentMap combined, DataComponentMap previousMap) {
        return combined.stream().anyMatch(typed -> {
            if (typed.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : (List<EffectAPIEffect>) list) {
                    if (effect.paramSet() == paramSet && effect.isActive(contexts.get(effect)) && previousMap.stream().map(c -> ((List<?>) c.value())).noneMatch(cs -> cs.contains(effect))) {
                        added = effect;
                        return true;
                    } else if (effect.paramSet() == paramSet && !effect.isActive(contexts.get(effect)) && previousMap.stream().map(c -> ((List<?>) c.value())).anyMatch(cs -> cs.contains(effect))) {
                        removed = effect;
                        return true;
                    } else
                        UNCHANGED.add(effect);
                }
            return false;
        });
    }

    public static DataComponentMap generateActiveEffects(Map<EffectAPIEffect, LootContext> contexts, LootContextParamSet paramSet,
                                                         DataComponentMap map, DataComponentMap previousMap) {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();

        for (TypedDataComponent<?> component : map) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>) list)) {
                    if (removed == effect) {
                        effect.onRemoved(contexts.get(effect));
                        continue;
                    }
                    if (added == effect) {
                        effect.onAdded(contexts.get(effect));
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                        continue;
                    }
                    if (UNCHANGED.contains(effect)) {
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                        continue;
                    }
                    if (effect.paramSet() == paramSet) {
                        if (effect.isActive(contexts.get(effect))) {
                            newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                            if (previousMap.stream().map(c -> ((List<?>) c.value())).noneMatch(cs -> cs.contains(effect)))
                                effect.onAdded(contexts.get(effect));
                        } else {
                            if (previousMap.stream().map(c -> ((List<?>) c.value())).anyMatch(cs -> cs.contains(effect)))
                                effect.onRemoved(contexts.get(effect));
                        }
                    }
                }
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());

        clearChangedCache();

        return builder.build();
    }

}
