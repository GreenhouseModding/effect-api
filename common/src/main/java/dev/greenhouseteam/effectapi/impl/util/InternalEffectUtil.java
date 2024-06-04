package dev.greenhouseteam.effectapi.impl.util;

import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class InternalEffectUtil {
    public static void executeOnAllEffects(DataComponentMap map, Consumer<EffectAPIEffect> consumer) {
        for (var entry : map)
            if (entry.type() == EffectAPIEffectTypes.ENTITY_TICK && entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> consumer.accept((EffectAPIEffect)effect));
    }

    public static <T extends EffectAPIEffect> T castConditional(EffectAPIEffect effect) {
        return (T) ((EffectAPIConditionalEffect)effect).effect();
    }

    public static DataComponentMap generateActiveEffects(LootContext context, LootContextParamSet paramSet, DataComponentMap map) {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();

        for (TypedDataComponent<?> component : map) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>) list))
                    if (effect.paramSet() == paramSet && effect.isActive(context))
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet()) {
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static boolean hasUpdatedActives(Entity entity, DataComponentMap newMap, DataComponentMap oldMap) {
        List<?> oldValues = oldMap.stream().flatMap(component -> ((List<?>)component.value()).stream()).toList();
        List<?> newValues = newMap.stream().flatMap(component -> ((List<?>)component.value()).stream()).toList();

        if (oldValues.equals(newValues))
            return false;

        newValues.stream().filter(object -> !oldValues.contains(object)).forEach(value -> {
            if (value instanceof EffectAPIEffect effect)
                if (effect.paramSet() == EffectAPILootContextParamSets.ENTITY)
                    effect.onAdded(createEntityOnlyContext(entity));
        });
        oldValues.stream().filter(object -> !newValues.contains(object)).forEach(value -> {
            if (value instanceof EffectAPIEffect effect)
                if (effect.paramSet() == EffectAPILootContextParamSets.ENTITY)
                    effect.onRemoved(createEntityOnlyContext(entity));
        });

        return true;
    }

    public static LootContext createEntityOnlyContext(Entity entity) {
        if (entity.level().isClientSide())
            return null;
        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        return new LootContext.Builder(params.create(EffectAPILootContextParamSets.ENTITY)).create(Optional.empty());
    }
}
