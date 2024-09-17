package house.greenhouse.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.effect.TriggerEffect;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public record EntityTriggerEffect(Optional<EffectAPIAction> onAdded, Optional<EffectAPIAction> onRemoved, Optional<EffectAPIAction> onActivated, Optional<EffectAPIAction> onDeactivated, Optional<EffectAPIAction> onRefresh) implements TriggerEffect {
    public static final Codec<EntityTriggerEffect> CODEC = TriggerEffect.codec(
            EffectAPIEntityActionTypes.CODEC, EntityTriggerEffect::new);

    @Override
    public EffectType<?, Entity> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TRIGGER;
    }
}
