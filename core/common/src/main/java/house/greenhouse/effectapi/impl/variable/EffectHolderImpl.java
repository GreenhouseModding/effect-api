package house.greenhouse.effectapi.impl.variable;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.EffectHolder;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import net.minecraft.core.component.DataComponentType;

public class EffectHolderImpl<E extends EffectAPIEffect> extends VariableHolderImpl<EffectAPIEffect> implements EffectHolder<E> {
    private DataComponentType<E> effectType;

    public EffectHolderImpl(DataComponentType<E> type, VariableHolder<EffectAPIEffect> holder) {
        super(((VariableHolderImpl)holder).getInnerCodec(), holder.getVariables(), holder.getRawJson());
        this.effectType = type;
    }

    public DataComponentType<E> effectType() {
        return effectType;
    }
}