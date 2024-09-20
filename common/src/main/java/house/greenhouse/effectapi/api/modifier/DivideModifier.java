package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DivideModifier(double amount) implements Modifier {
    public static final MapCodec<DivideModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(DivideModifier::amount)
    ).apply(inst, DivideModifier::new));

    @Override
    public double modify(double total) {
        return total / amount;
    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}