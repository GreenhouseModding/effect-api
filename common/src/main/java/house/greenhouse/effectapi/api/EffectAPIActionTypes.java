package house.greenhouse.effectapi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.action.EnchantmentEntityEffectAction;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParamSets;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.action.AllOfEntityAction;
import house.greenhouse.effectapi.impl.action.RandomEntityAction;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.Registry;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.function.Function;

public class EffectAPIActionTypes {
    public static final Codec<EffectAPIAction> CODEC = EffectAPIActionTypes.codec(EffectAPIRegistries.ACTION_TYPE, EffectAPILootContextParamSets.ENTITY);

    public static void registerAll(RegistrationCallback<MapCodec<? extends EffectAPIAction>> callback) {
        callback.register(EffectAPIRegistries.ACTION_TYPE, EffectAPI.asResource("all_of"), AllOfEntityAction.CODEC);
        callback.register(EffectAPIRegistries.ACTION_TYPE, EffectAPI.asResource("enchantment_entity_effect"), EnchantmentEntityEffectAction.CODEC);
        callback.register(EffectAPIRegistries.ACTION_TYPE, EffectAPI.asResource("random"), RandomEntityAction.CODEC);
    }

    public static Codec<EffectAPIAction> codec(Registry<MapCodec<? extends EffectAPIAction>> registry, LootContextParamSet paramSet) {
        return registry.byNameCodec().dispatch(EffectAPIAction::codec, Function.identity())
                .validate(
                        action -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            if (!paramSet.getAllowed().containsAll(action.requiredParams()))
                                collector.report("Parameters " + action.requiredParams().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided in this context");

                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success(action);
                            return DataResult.error(() -> "Validation error in Effect API action:" + collector.getReport().orElse("Unknown error."));
                        }
                );
    }
}
