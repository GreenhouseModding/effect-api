
package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class EffectAPIConditionalEffect<T extends EffectAPIEffect> implements EffectAPIEffect {
    private static Codec<LootItemCondition> conditionCodec(LootContextParamSet paramSet) {
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

    /**
     * Creates a codec that wraps a specified effect within a conditional effect, making it conditional.
     * @param codec     The codec of the inner effect.
     * @param paramSet  The param set for validating the condition, should be the same as the param set of the effect.
     * @return          A conditional effect codec.
     * @param <T>       The inner effect class.
     */
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

    @Override
    public void onActivated(LootContext context) {
        effect.onActivated(context);
    }

    @Override
    public void onDeactivated(LootContext context) {
        effect.onDeactivated(context);
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
    public boolean isActive(LootContext context, int tickCount) {
        if (tickCount % checkRate == 0) {
            previousValue = requirements.isEmpty() || requirements.get().test(context);
        }
        return previousValue;
    }

    @Override
    public void tick(LootContext context, int tickCount) {
        if (effect.shouldTick(context, previousValue && effect.isActive(context, tickCount), tickCount))
            effect.tick(context, tickCount);
    }

    @Override
    public boolean shouldTick(LootContext context, boolean isActive, int tickCount) {
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
}