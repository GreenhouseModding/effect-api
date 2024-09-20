package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MinModifier(double min) implements Modifier {
    public static final MapCodec<MinModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("min").forGetter(MinModifier::min)
    ).apply(inst, MinModifier::new));

    @Override
    public double modify(double total) {
        return Math.min(total, min);
    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}