package house.greenhouse.effectapi.impl.util;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.EffectHolder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class InternalEffectUtil {
    public static void executeOnAllEffects(DataComponentMap map, Consumer<EffectAPIEffect> consumer) {
        executeOnAllEffects(map, consumer, effect -> true);
    }

    public static void executeOnAllEffects(DataComponentMap map, Consumer<EffectAPIEffect> consumer, Predicate<EffectAPIEffect> predicate) {
        for (var entry : map)
            if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> {
                    EffectAPIEffect castEffect = (EffectAPIEffect)effect;
                    if (predicate.test(castEffect))
                        consumer.accept(castEffect);
                });
    }

    public static <E extends EffectAPIEffect> Optional<DataComponentMap> generateActiveEffectsIfNecessary(Map<EffectHolder<EffectAPIEffect>, LootContext> contexts,
                                                                              @Nullable Map<EffectHolder<E>, E> previousHolders,
                                                                              Map<EffectAPIEffect, EffectHolder<EffectAPIEffect>> reverseLookup,
                                                                              DataComponentMap combined, DataComponentMap previousMap,
                                                                              int tickCount) {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();
        boolean createNewMap = previousMap.stream().anyMatch(typedDataComponent -> ((List<?>) typedDataComponent.value()).stream().noneMatch(o -> ((List<?>) combined.getOrDefault(typedDataComponent.type(), List.of())).contains(o)));

        for (TypedDataComponent<?> component : combined) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>) list)) {
                    EffectHolder<EffectAPIEffect> holder = reverseLookup.get(effect);
                    LootContext context = contexts.get(holder);
                    if (previousHolders != null && previousHolders.containsKey(holder)) {
                        boolean active = effect.isActive(context, tickCount);
                        effect.onChanged(context, previousHolders.get(holder), active);
                        if (active) {
                            newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                        }
                        createNewMap = true;
                        continue;
                    }

                    if (effect.isActive(context, tickCount)) {
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                        if (previousMap.stream().map(c -> ((List<?>) c.value())).noneMatch(ls -> ls.contains(effect))) {
                            effect.onActivated(context);
                            createNewMap = true;
                        }
                    } else {
                        if (previousMap.stream().map(c -> ((List<?>) c.value())).anyMatch(ls -> ls.contains(effect))) {
                            effect.onDeactivated(context);
                            createNewMap = true;
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
