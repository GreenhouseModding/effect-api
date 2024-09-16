package house.greenhouse.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.effect.TriggerEffect;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Optional;

public record EntityTriggerEffect(Optional<EffectAPIAction> onAdded, Optional<EffectAPIAction> onRemoved, Optional<EffectAPIAction> onActivated, Optional<EffectAPIAction> onDeactivated, Optional<EffectAPIAction> onRefresh) implements TriggerEffect {
    public static final Codec<EntityTriggerEffect> CODEC = TriggerEffect.codec(
            EffectAPIEntityActionTypes.CODEC, EntityTriggerEffect::new);

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TRIGGER;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
