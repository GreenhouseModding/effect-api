package house.greenhouse.effectapi.api.effect;

import house.greenhouse.effectapi.api.variable.VariableHolder;

public interface EffectHolder<E extends EffectAPIEffect, T> extends VariableHolder<E> {
    EffectType<E, T> effectType();
}
