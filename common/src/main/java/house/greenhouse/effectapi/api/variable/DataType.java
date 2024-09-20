package house.greenhouse.effectapi.api.variable;

import com.mojang.serialization.Codec;

public record DataType<T>(Codec<T> codec, T validationValue) {}
