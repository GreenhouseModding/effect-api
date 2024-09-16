package house.greenhouse.effectapi.api;
import house.greenhouse.effectapi.api.variable.Variable;
import net.minecraft.core.Registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class EffectAPIVariableTypes {
    public static Codec<Variable<?>> codec(Registry<MapCodec<? extends Variable<?>>> registry, LootContextParamSet paramSet) {
        return registry.byNameCodec().<Variable<?>>dispatch("effect_api:variable_type", Variable::codec, mapCodec -> mapCodec)
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
}
