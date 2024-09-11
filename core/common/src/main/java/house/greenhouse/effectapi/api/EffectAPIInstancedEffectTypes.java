package house.greenhouse.effectapi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import house.greenhouse.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.function.Function;

public class EffectAPIInstancedEffectTypes {
    private static final Codec<EffectAPIInstancedEffect> DIRECT_CODEC = EffectAPIRegistries.INSTANCED_EFFECT_TYPE.byNameCodec().dispatch(EffectAPIInstancedEffect::codec, Function.identity());

    public static Codec<EffectAPIInstancedEffect> codec(LootContextParamSet paramSet) {
        return DIRECT_CODEC
                .validate(
                        effect -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            if (!paramSet.getAllowed().containsAll(effect.requiredParams()))
                                collector.report("Parameters " + effect.requiredParams().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided in this context");

                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success(effect);
                            return DataResult.error(() -> "Validation error in Effect API effect condition:" + collector.getReport().orElse("Unknown error."));
                        }
                );
    }
}
