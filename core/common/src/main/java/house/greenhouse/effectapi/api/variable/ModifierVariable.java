package house.greenhouse.effectapi.api.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.EffectAPIDataTypes;
import house.greenhouse.effectapi.api.EffectAPIModifierTypes;
import house.greenhouse.effectapi.api.EffectAPIVariableTypes;
import house.greenhouse.effectapi.api.modifier.Modifier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public abstract class ModifierVariable implements Variable<Double> {
    public static <T extends ModifierVariable> MapCodec<T> codec(Registry<MapCodec<? extends Variable<?>>> variableRegistry, LootContextParamSet paramSet, BiFunction<Either<Double, Variable<Number>>, List<Modifier>, T> constructor) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.either(Codec.DOUBLE, EffectAPIVariableTypes.codec(variableRegistry, paramSet).flatXmap(variable -> {
                    if (variable.dataType().validationValue() instanceof Number)
                        return DataResult.success((Variable<Number>)variable);
                    return DataResult.error(() -> "Base field of modifier variable type must be either a standalone number or a number based variable.");
                }, DataResult::success)).fieldOf("base").forGetter(ModifierVariable::base),
                EffectAPIModifierTypes.CODEC.listOf().fieldOf("modifiers").forGetter(ModifierVariable::modifiers)
        ).apply(inst, constructor));
    }

    private Either<Double, Variable<Number>> base;
    private List<Modifier> modifiers;

    public ModifierVariable(Either<Double, Variable<Number>> base, List<Modifier> modifiers) {
        this.base = base;
        this.modifiers = modifiers;
    }

    @Override
    public Double get(LootContext context) {
        double value = base.map(aDouble -> aDouble, variable -> variable.get(context).doubleValue());
        for (Modifier modifier : modifiers)
            value = modifier.modify(value);
        return value;
    }

    public Either<Double, Variable<Number>> base() {
        return base;
    }

    public List<Modifier> modifiers() {
        return modifiers;
    }

    @Override
    public Collection<LootContextParam<?>> requiredParams() {
        return base.map(aDouble -> List.of(), Variable::requiredParams);
    }

    @Override
    public DataType<Double> dataType() {
        return EffectAPIDataTypes.DOUBLE;
    }
}
