package house.greenhouse.test;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.RegistryFixedCodec;

public record DataEffect(DataComponentMap effects) {
    public static final Codec<DataEffect> DIRECT_CODEC = EffectAPIEntityEffectTypes.VARIABLE_ALLOWED_CODEC.xmap(DataEffect::new, DataEffect::effects);
    public static final Codec<DataEffect> NETWORK_DIRECT_CODEC = EffectAPIEntityEffectTypes.VARIABLE_ALLOWED_NETWORK_CODEC.xmap(DataEffect::new, DataEffect::effects);
    public static final Codec<Holder<DataEffect>> CODEC = RegistryFixedCodec.create(EffectAPIEntityTest.DATA_EFFECT);
}
