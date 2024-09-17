package house.greenhouse.effectapi.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.effect.EffectHolderImpl;
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
    public static <T> Codec<DataComponentMap> codec(Registry<EffectType<?, T>> typeRegistry) {
        return Codec.lazyInitialized(() -> Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> type.codec().listOf()).flatXmap(dataComponentMap -> encodeComponents(dataComponentMap), map -> (DataResult) decodeComponents(map)));
    }

    public static <T> Codec<DataComponentMap> variableAllowedCodec(Registry<EffectType<?, T>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodec(type.codec(), EffectAPIVariableTypes.validatedCodec(paramSet)).listOf()).flatXmap(dataComponentMap -> {
            var map = dataComponentMap.entrySet().stream().map(entry ->
                    Pair.of(entry.getKey(), entry.getValue().stream().map(variableHolder -> new EffectHolderImpl(entry.getKey(), variableHolder)).toList())
            ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
            return encodeComponents((Map<EffectType<?, T>, ?>)(Map<?, ?>) map);
        }, map -> (DataResult)decodeComponents(map));
    }

    public static <T> Codec<DataComponentMap> variableAllowedNetworkCodec(Registry<EffectType<?, T>> typeRegistry, LootContextParamSet paramSet) {
        return Codec.dispatchedMap(typeRegistry.byNameCodec(), type -> VariableHolder.wrapCodecForNetwork(type.codec(), EffectAPIVariableTypes.validatedCodec(paramSet)).listOf()).flatXmap(dataComponentMap -> encodeComponents(dataComponentMap), map -> (DataResult)decodeComponents(map));
    }

    @ApiStatus.Internal
    private static <T> DataResult<DataComponentMap> encodeComponents(Map<EffectType<?, T>, ?> componentTypes) {
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
