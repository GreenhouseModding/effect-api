package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AddModifier(double amount) implements Modifier {
    public static final MapCodec<AddModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("amount").forGetter(AddModifier::amount)
    ).apply(inst, AddModifier::new));

    @Override
    public double modify(double total) {
        return total + amount;
    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}
