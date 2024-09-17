package house.greenhouse.effectapi.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.variable.EffectHolderImpl;
import house.greenhouse.effectapi.mixin.DataComponentMapBuilderAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.stream.Collectors;

public class EffectAPIEffectTypes {
    public static Codec<DataComponentMap> codec(Registry<DataComponentType<?>> typeRegistry) {
        return Codec.lazyInitialized(() -> Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> type.codecOrThrow().listOf()).flatXmap(dataComponentMap -> encodeComponents(dataComponentMap), map -> (DataResult) decodeComponents(map)));
    }

    public static Codec<DataComponentMap> variableAllowedCodec(Registry<DataComponentType<?>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodec(type.codecOrThrow(), EffectAPIVariableTypes.validatedCodec(paramSet)).listOf()).flatXmap(dataComponentMap -> {
            var map = dataComponentMap.entrySet().stream().map(entry ->
                    Pair.of(entry.getKey(), entry.getValue().stream().map(variableHolder -> new EffectHolderImpl<>((DataComponentType<EffectAPIEffect>) entry.getKey(), (VariableHolder<EffectAPIEffect>) variableHolder)).toList())
            ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            return encodeComponents((Map<DataComponentType<?>, ?>)(Map<?, ?>) map);
        }, map -> (DataResult)decodeComponents(map));
    }

    public static Codec<DataComponentMap> variableAllowedNetworkCodec(Registry<DataComponentType<?>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodecForNetwork(type.codecOrThrow(), EffectAPIVariableTypes.validatedCodec(paramSet)).listOf()).flatXmap(dataComponentMap -> encodeComponents(dataComponentMap), map -> (DataResult)decodeComponents(map));
    }

    @ApiStatus.Internal
    private static DataResult<DataComponentMap> encodeComponents(Map<DataComponentType<?>, ?> componentTypes) {
        if (componentTypes.isEmpty())
            return DataResult.success(DataComponentMap.EMPTY);
        return DataResult.success(DataComponentMapBuilderAccessor.effect_api$invokeBuildFromMapTrusted((Map) componentTypes));
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
