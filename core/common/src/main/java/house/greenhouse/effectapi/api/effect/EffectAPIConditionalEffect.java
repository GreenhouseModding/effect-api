
package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.mixin.LootContextAccessor;
import house.greenhouse.effectapi.mixin.LootParamsAccessor;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class EffectAPIConditionalEffect<T extends EffectAPIEffect> implements EffectAPIEffect {
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
                conditionCodec(paramSet).optionalFieldOf("requirements").forGetter(EffectAPIConditionalEffect::requirements),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("check_rate", 1).forGetter(EffectAPIConditionalEffect::checkRate)
        ).apply(inst, (t1, t2, t3) -> new EffectAPIConditionalEffect<>(t1, t2, t3, paramSet)));
    }

    private final T effect;
    private final Optional<LootItemCondition> requirements;
    private final int checkRate;
    private final LootContextParamSet paramSet;

    private WeakHashMap<LootContext, Long> ticks = new WeakHashMap<>(64);
    private boolean previousValue = false;

    public EffectAPIConditionalEffect(T effect, Optional<LootItemCondition> requirements, int checkRate, LootContextParamSet paramSet) {
        this.effect = effect;
        this.requirements = requirements;
        this.checkRate = checkRate;
        this.paramSet = paramSet;
    }

    public T effect() {
        return effect;
    }

    public Optional<LootItemCondition> requirements() {
        return requirements;
    }

    public int checkRate() {
        return checkRate;
    }

    public boolean previousValue() {
        return previousValue;
    }

    @Override
    public void onAdded(LootContext context) {
        effect.onAdded(context);
    }

    @Override
    public void onRemoved(LootContext context) {
        effect.onRemoved(context);
    }

    @Override
    public void onRefreshed(LootContext context) {
        effect.onRefreshed(context);
    }

    @Override
    public boolean isActive(LootContext context) {
        if (getTicks(context) % checkRate == 0) {
            previousValue = requirements.isEmpty() || requirements.get().test(context);
        }
        return previousValue;
    }

    @Override
    public void tick(LootContext context) {
        ticks.computeIfPresent(context, (ctx, l) -> l + 1);
        if (effect.shouldTick(context, previousValue))
            effect.tick(context);
    }

    @Override
    public boolean shouldTick(LootContext context, boolean isActive) {
        return true;
    }

    @Override
    public DataComponentType<?> type() {
        return effect.type();
    }

    @Override
    public LootContextParamSet paramSet() {
        return paramSet;
    }

    private long getTicks(LootContext context) {
        if (ticks.keySet().stream().noneMatch(context1 -> context1 == context)) {
            ticks.put(context, 0L);
        }
        return ticks.get(context);
    }
}