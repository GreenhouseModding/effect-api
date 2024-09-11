package house.greenhouse.test;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.RegistryFixedCodec;

public record Power(DataComponentMap effects) {
    public static final Codec<Power> DIRECT_CODEC = EffectAPIEffectTypes.codec(EffectAPIEntityLootContextParamSets.ENTITY).xmap(Power::new, Power::effects);
    public static final Codec<Holder<Power>> CODEC = RegistryFixedCodec.create(EffectAPITest.POWER);
}
