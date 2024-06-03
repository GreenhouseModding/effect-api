
package dev.greenhouseteam.effectapi.api.effect;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.params.EffectAPILootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record EffectAPIConditionalEffect<T extends EffectAPIEffect>(T effect, Optional<LootItemCondition> requirements) implements EffectAPIEffect {
    public static Codec<LootItemCondition> conditionCodec(LootContextParamSet paramSet) {
        return LootItemCondition.DIRECT_CODEC
                .validate(
                        loot -> {
                            ProblemReporter.Collector collector = new ProblemReporter.Collector();
                            var difference = Sets.difference(paramSet.getAllowed(), loot.getReferencedContextParams());
                            if (!difference.isEmpty()) {
                                collector.report("Parameters " + difference + " are not provided in this context");
                            }
                            var map = collector.get();
                            if (map.isEmpty())
                                return DataResult.success(loot);
                            return DataResult.error(() -> "Validation error in Effect API effect condition: " + loot);
                        }
                );
    }

    public static <T extends EffectAPIEffect> Codec<EffectAPIConditionalEffect<T>> codec(Codec<T> $$0, LootContextParamSet $$1) {
        return RecordCodecBuilder.create(
                $$2 -> $$2.group(
                                $$0.fieldOf("effect").forGetter(EffectAPIConditionalEffect::effect),
                                conditionCodec($$1).optionalFieldOf("requirements").forGetter(EffectAPIConditionalEffect::requirements)
                        )
                        .apply($$2, EffectAPIConditionalEffect::new)
        );
    }


    @Override
    public void onAdded(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null && isActive(entity))
            effect.onAdded(lootContext);
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null && isActive(entity))
            effect.onRemoved(lootContext);
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPILootContextParamSets.ENTITY;
    }

    @Override
    public DataComponentType<?> type() {
        return effect.type();
    }

    public boolean isActive(Entity entity) {
        if (entity.level().isClientSide())
            return false;
        if (requirements.isEmpty())
            return true;
        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        return requirements.get().test(new LootContext.Builder(params.create(EffectAPILootContextParamSets.ENTITY)).create(Optional.empty()));
    }
}
