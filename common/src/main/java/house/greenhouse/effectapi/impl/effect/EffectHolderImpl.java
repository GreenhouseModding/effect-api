package house.greenhouse.effectapi.impl.effect;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.variable.VariableHolderImpl;

public class EffectHolderImpl<E extends EffectAPIEffect, T> extends VariableHolderImpl<E> implements EffectHolder<E> {
    private final EffectType<E> effectType;

    public EffectHolderImpl(EffectType<E> type, VariableHolder<E> holder) {
        super(((VariableHolderImpl<E>)holder).getInnerCodec(), holder.getVariables(), holder.getRawJson());
        this.effectType = type;
    }

    public EffectType<E> effectType() {
        return effectType;
    }
}