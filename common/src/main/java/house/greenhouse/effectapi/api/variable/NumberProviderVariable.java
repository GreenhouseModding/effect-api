package house.greenhouse.effectapi.api.variable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.impl.registry.EffectAPIDataTypes;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.Collection;

public record NumberProviderVariable(NumberProvider provider) implements Variable<Float> {
    public static final MapCodec<NumberProviderVariable> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            NumberProviders.CODEC.fieldOf("provider").forGetter(NumberProviderVariable::provider)
    ).apply(inst, NumberProviderVariable::new));

    @Override
    public Float get(LootContext context) {
        return provider.getFloat(context);
    }

    public NumberProvider provider() {
        return provider;
    }

    @Override
    public Collection<LootContextParam<?>> requiredParams() {
        return provider.getReferencedContextParams();
    }

    @Override
    public MapCodec<? extends Variable<?>> codec() {
        return CODEC;
    }

    @Override
    public DataType<Float> dataType() {
        return EffectAPIDataTypes.FLOAT;
    }
}
