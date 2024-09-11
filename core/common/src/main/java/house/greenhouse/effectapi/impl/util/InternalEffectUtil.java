package house.greenhouse.effectapi.impl.util;

import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
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

    public static <T extends EffectAPIEffect> T castConditional(EffectAPIEffect effect) {
        return (T) ((EffectAPIConditionalEffect)effect).effect();
    }

    private static final List<EffectAPIEffect> EFFECTS_TO_SKIP = new ArrayList<>();

    public static void clearEffectsToSkip() {
        EFFECTS_TO_SKIP.clear();
    }

    public static boolean hasNewActives(LootContext context, LootContextParamSet paramSet,
                                        DataComponentMap combined, DataComponentMap previousMap,
                                        Map<EffectAPIEffect, ResourceLocation> sources) {
        return combined.stream().anyMatch(typed -> {
            if (typed.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : (List<EffectAPIEffect>) list) {
                    LootParams.Builder paramBuilder = LootContextUtil.copyIntoParamBuilder(context);
                    if (paramSet.isAllowed(EffectAPILootContextParams.SOURCE) && !context.hasParam(EffectAPILootContextParams.SOURCE))
                        paramBuilder.withOptionalParameter(EffectAPILootContextParams.SOURCE, sources.get(effect));
                    LootContext context1 = new LootContext.Builder(paramBuilder.create(paramSet)).create(Optional.empty());
                    if (effect.paramSet() == paramSet && effect.isActive(context1))
                        return true;
                    else if (previousMap.stream().map(c -> ((List<?>) c.value())).anyMatch(cs -> cs.contains(effect)))
                        return true;
                    else
                        EFFECTS_TO_SKIP.add(effect);
                }
            return false;
        });
    }

    public static DataComponentMap generateActiveEffects(LootContext context, LootContextParamSet paramSet,
                                                         DataComponentMap map, DataComponentMap previousMap,
                                                         Map<EffectAPIEffect, ResourceLocation> sources) {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();

        for (TypedDataComponent<?> component : map) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>) list)) {
                    if (EFFECTS_TO_SKIP.contains(effect))
                        continue;
                    LootParams.Builder paramBuilder = LootContextUtil.copyIntoParamBuilder(context);
                    if (paramSet.isAllowed(EffectAPILootContextParams.SOURCE) && !context.hasParam(EffectAPILootContextParams.SOURCE))
                        paramBuilder.withOptionalParameter(EffectAPILootContextParams.SOURCE, sources.get(effect));
                    LootContext context1 = new LootContext.Builder(paramBuilder.create(paramSet)).create(Optional.empty());
                    if (effect.paramSet() == paramSet && effect.isActive(context1)) {
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                        if (previousMap.stream().map(c -> ((List<?>) c.value())).noneMatch(cs -> cs.contains(effect)))
                            effect.onAdded(context1);
                    } else if (previousMap.stream().map(c -> ((List<?>) c.value())).anyMatch(cs -> cs.contains(effect)))
                        effect.onRemoved(context1);
                }
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());

        EFFECTS_TO_SKIP.clear();

        return builder.build();
    }

}
