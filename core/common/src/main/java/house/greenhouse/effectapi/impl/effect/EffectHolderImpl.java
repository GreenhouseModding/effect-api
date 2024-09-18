package house.greenhouse.effectapi.impl.effect;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.variable.VariableHolderImpl;

public class EffectHolderImpl<E extends EffectAPIEffect, T> extends VariableHolderImpl<E> implements EffectHolder<E, T> {
    private final EffectType<E, T> effectType;

    public EffectHolderImpl(EffectType<E, T> type, VariableHolder<E> holder) {
        super(((VariableHolderImpl<E>)holder).getInnerCodec(), holder.getVariables(), holder.getRawJson());
        this.effectType = type;
    }

    public EffectType<E, T> effectType() {
        return effectType;
    }
}