
package house.greenhouse.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.effect.TickEffect;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public record EntityTickEffect<T extends EffectAPIAction>(T effect) implements TickEffect<T> {
    public static final Codec<EntityTickEffect<?>> CODEC = EffectAPIEntityActionTypes.CODEC.xmap(EntityTickEffect::new, EntityTickEffect::effect);

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TICK;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
