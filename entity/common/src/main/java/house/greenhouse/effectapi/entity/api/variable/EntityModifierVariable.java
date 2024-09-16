package house.greenhouse.effectapi.entity.api.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.api.variable.ModifierVariable;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityRegistries;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;

import java.util.List;

public class EntityModifierVariable extends ModifierVariable {
    public static final MapCodec<EntityModifierVariable> CODEC = ModifierVariable.codec(EffectAPIEntityRegistries.VARIABLE, EffectAPIEntityLootContextParamSets.ENTITY,
            EntityModifierVariable::new);

    public EntityModifierVariable(Either<Double, Variable<Number>> base, List<Modifier> modifiers) {
        super(base, modifiers);
    }

    @Override
    public MapCodec<? extends Variable<?>> codec() {
        return CODEC;
    }
}
