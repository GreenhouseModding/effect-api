
package house.greenhouse.effectapi.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.api.EffectAPICodecs;
import house.greenhouse.effectapi.impl.registry.EffectAPIEffectTypes;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.api.effect.TickEffect;

public record EntityTickEffect<A extends EffectAPIAction>(A action) implements TickEffect<A> {
    public static final Codec<EntityTickEffect<?>> CODEC = EffectAPICodecs.ACTION.xmap(EntityTickEffect::new, EntityTickEffect::action);

    @Override
    public EffectType<?> type() {
        return EffectAPIEffectTypes.TICK;
    }
}
