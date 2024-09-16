package house.greenhouse.effectapi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.mixin.DataComponentMapBuilderAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public class EffectAPIEffectTypes {
    public static Codec<DataComponentMap> codec(Registry<DataComponentType<?>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.lazyInitialized(() -> Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> type.codecOrThrow().listOf()).flatXmap(dataComponentMap -> encodeComponents(typeRegistry, null, paramSet, dataComponentMap), map -> (DataResult) decodeComponents(map)));
    }

    public static Codec<DataComponentMap> variableAllowedCodec(Registry<DataComponentType<?>> typeRegistry, Registry<MapCodec<? extends Variable<?>>> variableRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodec(type.codecOrThrow(), EffectAPIVariableTypes.codec(variableRegistry, paramSet)).listOf()).flatXmap(dataComponentMap -> encodeComponents(typeRegistry, variableRegistry, paramSet, dataComponentMap), map -> (DataResult) decodeComponents(map));
    }

    public static Codec<DataComponentMap> variableAllowedNetworkCodec(Registry<DataComponentType<?>> typeRegistry, Registry<MapCodec<? extends Variable<?>>> variableRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodecForNetwork(type.codecOrThrow(), EffectAPIVariableTypes.codec(variableRegistry, paramSet)).listOf()).flatXmap(dataComponentMap -> encodeComponents(typeRegistry, variableRegistry, paramSet, dataComponentMap), map -> (DataResult)decodeComponents(map));
    }

    @ApiStatus.Internal
    private static DataResult<DataComponentMap> encodeComponents(Registry<DataComponentType<?>> typeRegistry, Registry<MapCodec<? extends Variable<?>>> variableRegistry, LootContextParamSet paramSet, Map<DataComponentType<?>, ?> componentTypes) {
        if (componentTypes.isEmpty())
            return DataResult.success(DataComponentMap.EMPTY);
        ProblemReporter.Collector collector = new ProblemReporter.Collector();
        for (var component : componentTypes.entrySet()) {
            if (variableRegistry != null) {
                for (var variable : ((List<?>) component.getValue()).stream().flatMap(object -> ((VariableHolder<?>) object).getVariables().values().stream()).filter(object -> object instanceof Variable<?> variable &&
                        !paramSet.getAllowed().containsAll(variable.requiredParams())).toList())
                    collector.report("Parameters [" + String.join(", ", variable.requiredParams().stream().filter(param -> !paramSet.isAllowed(param)).toList().stream().map(lootContextParam -> lootContextParam.getName().toString()).toList()) + "] are not provided for variable " + variableRegistry.getKey(variable.codec()) + ".");
            }
            for (var effect : ((List<?>) component.getValue()).stream().filter(object -> object instanceof EffectAPIEffect effect &&
                    !paramSet.getAllowed().containsAll(effect.paramSet().getRequired())).map(object -> (EffectAPIEffect) object).toList())
                collector.report("Parameters " + effect.paramSet().getRequired().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided for " + typeRegistry.getKey(effect.type()) + ".");
        }

        var errorMap = collector.get();
        if (errorMap.isEmpty())
            return DataResult.success(DataComponentMapBuilderAccessor.effectapi$invokeBuildFromMapTrusted((Map) componentTypes));
        return DataResult.error(() -> "Validation error in EffectAPI effect:" + collector.getReport().orElse("Unknown error."), DataComponentMap.EMPTY);
    }

    @ApiStatus.Internal
    private static DataResult<Map<DataComponentType<?>, Object>> decodeComponents(DataComponentMap map) {
        int size = map.size();
        if (size == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
        } else {
            Reference2ObjectMap<DataComponentType<?>, Object> map2 = new Reference2ObjectArrayMap<>(size);

            for (TypedDataComponent<?> component : map) {
                if (!component.type().isTransient()) {
                    map2.put(component.type(), component.value());
                }
            }
            return DataResult.success(map2);
        }
    }
}
