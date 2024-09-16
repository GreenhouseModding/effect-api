package house.greenhouse.effectapi.api.modifier;

import com.mojang.serialization.MapCodec;

public interface Modifier {
    double modify(double total);

    MapCodec<? extends Modifier> codec();
}
