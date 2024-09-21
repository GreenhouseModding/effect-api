package house.greenhouse.effectapi.api;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.EffectAPIEffectTypeInternals;
import house.greenhouse.effectapi.impl.effect.EffectHolderImpl;
import house.greenhouse.effectapi.impl.registry.EffectAPIRegistries;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EffectAPICodecs {
    public static final Codec<DataComponentMap> EFFECT_MAP = Codec.lazyInitialized(() -> Codec.dispatchedMap(EffectAPIRegistries.EFFECT_TYPE.byNameCodec(), type -> type.codec().listOf()).flatXmap(dataComponentMap -> EffectAPIEffectTypeInternals.encodeComponents(dataComponentMap), map -> (DataResult) EffectAPIEffectTypeInternals.decodeComponents(map)));
    public static final Codec<DataComponentMap> VARIABLE_EFFECT_MAP = Codec.dispatchedMap(EffectAPIRegistries.EFFECT_TYPE.byNameCodec(), type -> VariableHolder.wrapCodec(type.codec(), validatedVariableCodec(type.requiredParams())).listOf()).flatXmap(dataComponentMap -> {
        var map = dataComponentMap.entrySet().stream().map(entry ->
                Pair.of(entry.getKey(), entry.getValue().stream().map(variableHolder -> new EffectHolderImpl(entry.getKey(), variableHolder)).toList())
        ).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        return EffectAPIEffectTypeInternals.encodeComponents((Map<EffectType<?>, ?>) (Map<?, ?>) map);
    }, map -> (DataResult) EffectAPIEffectTypeInternals.decodeComponents(map));
    public static final Codec<DataComponentMap> VARIABLE_EFFECT_MAP_NETWORK = Codec.dispatchedMap(EffectAPIRegistries.EFFECT_TYPE.byNameCodec(), type -> VariableHolder.wrapCodecForNetwork(type.codec(), validatedVariableCodec(type.requiredParams())).listOf()).flatXmap(dataComponentMap -> EffectAPIEffectTypeInternals.encodeComponents(dataComponentMap), map -> (DataResult) EffectAPIEffectTypeInternals.decodeComponents(map));

    public static final Codec<Variable<?>> VARIABLE = EffectAPIRegistries.VARIABLE_TYPE.byNameCodec().dispatch("effect_api:variable_type", Variable::codec, mapCodec -> mapCodec);
    public static Codec<Variable<?>> validatedVariableCodec(Collection<LootContextParam<?>> paramSet) {
        return VARIABLE
                .validate(
                        variable -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            if (!paramSet.containsAll(variable.requiredParams()))
                                collector.report("Parameters " + variable.requiredParams().stream().filter(param -> !paramSet.contains(param)).toList() + " are not provided in this context");

                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success(variable);
                            return DataResult.error(() -> "Validation error in Effect API variable:" + collector.getReport().orElse("Unknown error."));
                        }
                );
    }

    public static final Codec<EffectAPIAction> ACTION = validatedActionCodec(EffectAPILootContextContents.ENTITY);
    public static Codec<EffectAPIAction> validatedActionCodec(LootContextParamSet paramSet) {
        return EffectAPIRegistries.ACTION_TYPE.byNameCodec().dispatch(EffectAPIAction::type, actionType -> (MapCodec<? extends EffectAPIAction>) actionType.codec(paramSet))
                .flatXmap(
                        action -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            if (!paramSet.getAllowed().containsAll(action.requiredParams()))
                                collector.report("Parameters " + action.requiredParams().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided in this context");

                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success((EffectAPIAction) action);
                            return DataResult.error(() -> "Validation error in Effect API action:" + collector.getReport().orElse("Unknown error."));
                        },
                        action -> (DataResult)DataResult.success(action)
                );
    }
    public static final Codec<Modifier> MODIFIER = EffectAPIRegistries.MODIFIER.byNameCodec().dispatch(Modifier::codec, Function.identity());
}
