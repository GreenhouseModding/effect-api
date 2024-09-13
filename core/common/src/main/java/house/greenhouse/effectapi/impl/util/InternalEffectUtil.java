package house.greenhouse.effectapi.impl.util;

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
import java.util.Optional;
import java.util.function.Consumer;

public class InternalEffectUtil {
    public static void executeOnAllEffects(DataComponentMap map, Consumer<EffectAPIEffect> consumer) {
        for (var entry : map)
            if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> consumer.accept((EffectAPIEffect)effect));
    }

    public static Optional<DataComponentMap> generateActiveEffectsIfNecessary(Map<EffectAPIEffect, LootContext> contexts, LootContextParamSet paramSet,
                                                                              DataComponentMap combined, DataComponentMap previousMap) {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();
        boolean createNewMap = false;

        for (TypedDataComponent<?> component : combined) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>) list)) {
                    if (effect.paramSet() == paramSet) {
                        LootContext context = contexts.get(effect);
                        if (effect.isActive(context)) {
                            newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                            if (previousMap.stream().map(c -> ((List<?>) c.value())).noneMatch(ls -> ls.contains(effect))) {
                                effect.onAdded(context);
                                createNewMap = true;
                            }
                        } else {
                            if (previousMap.stream().map(c -> ((List<?>) c.value())).anyMatch(ls -> ls.contains(effect))) {
                                effect.onRemoved(context);
                                createNewMap = true;
                            }
                        }
                    }
                }
        }

        if (!createNewMap)
            return Optional.empty();

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());

        return Optional.of(builder.build());
    }

}
