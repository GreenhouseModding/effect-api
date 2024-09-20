package house.greenhouse.effectapi.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.effect.TriggerEffect;
import house.greenhouse.effectapi.api.action.EffectAPIAction;

import java.util.Optional;

public record EntityTriggerEffect(Optional<EffectAPIAction> onAdded, Optional<EffectAPIAction> onRemoved, Optional<EffectAPIAction> onActivated, Optional<EffectAPIAction> onDeactivated, Optional<EffectAPIAction> onRefresh) implements TriggerEffect {
    public static final Codec<EntityTriggerEffect> CODEC = TriggerEffect.codec(
            EffectAPIActionTypes.CODEC, EntityTriggerEffect::new);

    @Override
    public EffectType<?> type() {
        return EffectAPIEffectTypes.TRIGGER;
    }
}
