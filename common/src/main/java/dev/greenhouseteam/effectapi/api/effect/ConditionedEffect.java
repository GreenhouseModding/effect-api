
package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIEffects;
import dev.greenhouseteam.effectapi.api.params.EffectsAPILootContextParamSets;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

import java.util.Optional;

public class ConditionedEffect extends Effect {
    public static final Codec<ConditionedEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Effect.CODEC.fieldOf("effects").forGetter(effect -> effect.map),
            LootItemConditions.DIRECT_CODEC.fieldOf("condition").forGetter(effect -> effect.condition)
    ).apply(inst, ConditionedEffect::new));

    private final DataComponentMap map;
    private final LootItemCondition condition;

    public ConditionedEffect(DataComponentMap map, LootItemCondition condition) {
        this.map = map;
        this.condition = condition;
        parentChildren();
    }

    @Override
    public void onAdded(Entity entity) {
        if (isActive(entity))
            childrenAsEffects().forEach(effect -> effect.onAdded(entity));
    }

    @Override
    public void onRemoved(Entity entity) {
        childrenAsEffects().forEach(effect -> effect.onRemoved(entity));
    }

    @Override
    public void tick(Entity entity) {
        if (isActive(entity))
            childrenAsEffects().forEach(effect -> effect.tick(entity));
    }

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEffects.CONDITIONED;
    }

    public boolean isActive(Entity entity) {
        if (entity.level().isClientSide())
            return false;
        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        return condition.test(new LootContext.Builder(params.create(EffectsAPILootContextParamSets.ENTITY)).create(Optional.empty()));
    }

    @Override
    public DataComponentMap children() {
        return map;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public Codec<? extends Effect> codec() {
        return CODEC;
    }
}
