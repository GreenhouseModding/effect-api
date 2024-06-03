
package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record EffectAPIConditionalEffect<T extends EffectAPIEffect>(T effect, Optional<LootItemCondition> requirements, LootContextParamSet paramSet) implements EffectAPIEffect {
    public static Codec<LootItemCondition> conditionCodec(LootContextParamSet paramSet) {
        return LootItemCondition.DIRECT_CODEC
                .validate(
                        loot -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            if (!paramSet.getAllowed().containsAll(loot.getReferencedContextParams()))
                                collector.report("Parameters " + loot.getReferencedContextParams().stream().filter(param -> !paramSet.isAllowed(param)).toList() + " are not provided in this context");

                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success(loot);
                            return DataResult.error(() -> "Validation error in Effect API effect condition:" + collector.getReport().orElse("Unknown error."));
                        }
                );
    }

    public static <T extends EffectAPIEffect> Codec<EffectAPIConditionalEffect<T>> codec(Codec<T> codec, LootContextParamSet paramSet) {
        return RecordCodecBuilder.create(inst -> inst.group(
                codec.fieldOf("effect").forGetter(EffectAPIConditionalEffect::effect),
                conditionCodec(paramSet).optionalFieldOf("requirements").forGetter(EffectAPIConditionalEffect::requirements)
        ).apply(inst, (t1, t2) -> new EffectAPIConditionalEffect<>(t1, t2, paramSet)));
    }


    @Override
    public void onAdded(LootContext lootContext) {
        if (isActive(lootContext))
            effect.onAdded(lootContext);
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        if (isActive(lootContext))
            effect.onRemoved(lootContext);
    }

    @Override
    public boolean isActive(LootContext context) {
        return requirements.isEmpty() || requirements.get().test(context);
    }

    @Override
    public DataComponentType<?> type() {
        return effect.type();
    }
}
