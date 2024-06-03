package dev.greenhouseteam.effectapi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIEnchantmentEntityEffect;
import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIInstancedEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
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

    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIInstancedEffect>> callback) {
        callback.register(EffectAPIRegistries.INSTANCED_EFFECT_TYPE, EffectAPI.asResource("enchantment_effect"), EffectAPIEnchantmentEntityEffect.CODEC);
    }
}
