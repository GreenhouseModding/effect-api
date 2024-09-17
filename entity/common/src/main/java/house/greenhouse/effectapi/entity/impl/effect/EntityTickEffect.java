
package house.greenhouse.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.effect.TickEffect;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import net.minecraft.world.entity.Entity;

public record EntityTickEffect<A extends EffectAPIAction>(A action) implements TickEffect<A> {
    public static final Codec<EntityTickEffect<?>> CODEC = EffectAPIEntityActionTypes.CODEC.xmap(EntityTickEffect::new, EntityTickEffect::action);

    @Override
    public EffectType<?, Entity> type() {
        return EffectAPIEntityEffectTypes.ENTITY_TICK;
    }
}
