package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MaxModifier(double max) implements Modifier {
    public static final MapCodec<MaxModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("max").forGetter(MaxModifier::max)
    ).apply(inst, MaxModifier::new));

    @Override
    public double modify(double total) {
        return Math.max(total, max);
    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}