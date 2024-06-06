package dev.greenhouseteam.effectapi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.mixin.DataComponentMapBuilderAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.List;
import java.util.Map;

public class EffectAPIEffectTypes {
    private static final Codec<DataComponentType<?>> COMPONENT_CODEC = EffectAPIRegistries.EFFECT_TYPE.byNameCodec();

    public static Codec<DataComponentMap> codec(LootContextParamSet paramSet) {
        return Codec.dispatchedMap(COMPONENT_CODEC, DataComponentType::codecOrThrow).flatXmap(dataComponentMap -> encodeComponents(paramSet, dataComponentMap), map -> (DataResult) decodeComponents(map));
    }

    private static DataResult<DataComponentMap> encodeComponents(LootContextParamSet paramSet, Map<DataComponentType<?>, ?> componentTypes) {
        if (componentTypes.isEmpty())
            return DataResult.success(DataComponentMap.EMPTY);
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        for (var component : componentTypes.entrySet())
            for (var effect : ((List<?>) component.getValue()).stream().filter(object -> object instanceof EffectAPIEffect effect &&
                    !paramSet.getAllowed().containsAll(effect.paramSet().getRequired())).map(object -> (EffectAPIEffect) object).toList())
                collector.report("Parameters " + effect.paramSet().getRequired().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided for " + EffectAPIRegistries.EFFECT_TYPE.getKey(effect.type()) + ".");

        var errorMap = collector.get();
        if (errorMap.isEmpty())
            return DataResult.success(DataComponentMapBuilderAccessor.effectapi$invokeBuildFromMapTrusted((Map<DataComponentType<?>, Object>) componentTypes));
        return DataResult.error(() -> "Validation error in Effect API effect:" + collector.getReport().orElse("Unknown error."), DataComponentMap.EMPTY);
    }

    private static DataResult<Map<DataComponentType<?>, Object>> decodeComponents(DataComponentMap map) {
        int $$1 = map.size();
        if ($$1 == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
        } else {
            Reference2ObjectMap<DataComponentType<?>, Object> $$2 = new Reference2ObjectArrayMap<>($$1);

            for (TypedDataComponent<?> $$3 : map) {
                if (!$$3.type().isTransient()) {
                    $$2.put($$3.type(), $$3.value());
                }
            }

            return DataResult.success($$2);
        }
    }
}
