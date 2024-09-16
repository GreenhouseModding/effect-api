package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ClampModifier(double min, double max) implements Modifier {
    public static final MapCodec<ClampModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.DOUBLE.fieldOf("min").forGetter(ClampModifier::min),
            Codec.DOUBLE.fieldOf("max").forGetter(ClampModifier::max)
    ).apply(inst, ClampModifier::new));

    @Override
    public double modify(double total) {
        return Math.clamp(total, min, max);
    }

    @Override
    public MapCodec<? extends Modifier> codec() {
        return CODEC;
    }
}