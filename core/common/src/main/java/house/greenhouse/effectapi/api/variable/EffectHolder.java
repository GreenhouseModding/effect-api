package house.greenhouse.effectapi.api.variable;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import net.minecraft.core.component.DataComponentType;

public interface EffectHolder<E extends EffectAPIEffect> extends VariableHolder<EffectAPIEffect> {
    DataComponentType<E> effectType();
}
