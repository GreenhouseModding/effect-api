package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MultiplyModifier(double amount) implements Modifier {
    public static final MapCodec<MultiplyModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(MultiplyModifier::amount)
    ).apply(inst, MultiplyModifier::new));

    @Override
    public double modify(double total) {
        return total * amount;
    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}