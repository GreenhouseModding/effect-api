package house.greenhouse.effectapi.api;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.variable.ModifierVariable;
import house.greenhouse.effectapi.api.variable.NumberProviderVariable;
import house.greenhouse.effectapi.api.variable.Variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class EffectAPIVariableTypes {
    public static final Codec<Variable<?>> CODEC = EffectAPIRegistries.VARIABLE_TYPE.byNameCodec().<Variable<?>>dispatch("effect_api:variable_type", Variable::codec, mapCodec -> mapCodec);

    public static Codec<Variable<?>> validatedCodec(LootContextParamSet paramSet) {
        return CODEC
                .validate(
                        variable -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            if (!paramSet.getAllowed().containsAll(variable.requiredParams()))
                                collector.report("Parameters " + variable.requiredParams().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided in this context");

                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success(variable);
                            return DataResult.error(() -> "Validation error in Effect API variable:" + collector.getReport().orElse("Unknown error."));
                        }
                );
    }

    public static void registerAll(RegistrationCallback<MapCodec<? extends Variable<?>>> callback) {
        callback.register(EffectAPIRegistries.VARIABLE_TYPE, EffectAPI.asResource("number_provider"), NumberProviderVariable.CODEC);
        callback.register(EffectAPIRegistries.VARIABLE_TYPE, EffectAPI.asResource("modifier"), ModifierVariable.CODEC);
    }
}
