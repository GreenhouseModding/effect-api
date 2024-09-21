package house.greenhouse.effectapi.api.variable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.EffectAPICodecs;
import house.greenhouse.effectapi.impl.registry.EffectAPIDataTypes;
import house.greenhouse.effectapi.impl.registry.EffectAPIModifierTypes;
import house.greenhouse.effectapi.impl.registry.EffectAPIVariableTypes;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.mixin.LootContextAccessor;
import house.greenhouse.effectapi.mixin.LootParamsAccessor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ModifierVariable implements Variable<Double> {
    public static final MapCodec<ModifierVariable> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.either(Codec.DOUBLE, EffectAPICodecs.VARIABLE.flatXmap(variable -> {
                if (variable.dataType().validationValue() instanceof Number)
                    return DataResult.success((Variable<Number>)variable);
                return DataResult.error(() -> "Base field of modifier variable type must be either a standalone number or a number based variable.");
                }, DataResult::success)).fieldOf("base").forGetter(ModifierVariable::base),
            EffectAPICodecs.MODIFIER.listOf().fieldOf("modifiers").forGetter(ModifierVariable::modifiers)
    ).apply(inst, ModifierVariable::new));

    private final Either<Double, Variable<Number>> base;
    private final List<Modifier> modifiers;
    private Optional<Boolean> hasChecked = Optional.empty();

    public ModifierVariable(Either<Double, Variable<Number>> base, List<Modifier> modifiers) {
        this.base = base;
        this.modifiers = modifiers;
    }

    @Override
    public Double get(LootContext context) {
        double value = base.map(aDouble -> aDouble, variable -> {
            if (hasChecked.isPresent() && !hasChecked.get())
                return 0.0;

            if (hasChecked.isEmpty()) {
                var missing = variable.requiredParams()
                        .stream()
                        .filter(param -> !((LootParamsAccessor)((LootContextAccessor)context).effect_api$getParams()).effect_api$getParams().containsKey(param))
                        .map(param -> param.getName().toString())
                        .toList();
                if (!missing.isEmpty()) {
                    EffectAPI.LOG.error("Modifier Variable context is missing params [{}].", String.join(", ", missing));
                    hasChecked = Optional.of(false);
                    return 0.0;
                }
            }
            hasChecked = Optional.of(true);
            return variable.get(context).doubleValue();
        });
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
    public MapCodec<? extends Variable<?>> codec() {
        return CODEC;
    }

    @Override
    public DataType<Double> dataType() {
        return EffectAPIDataTypes.DOUBLE;
    }
}
