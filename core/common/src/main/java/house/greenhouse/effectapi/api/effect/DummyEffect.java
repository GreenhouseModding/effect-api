package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public record DummyEffect(Registry<DataComponentType<?>> typeRegistry, ResourceKey<DataComponentType<?>> typeKey, LootContextParamSet paramSet) implements EffectAPIEffect {
    public static Codec<DummyEffect> codec(Registry<DataComponentType<?>> typeRegistry, ResourceLocation typeKey, LootContextParamSet paramSet) {
        return Codec.unit(() -> new DummyEffect(typeRegistry, ResourceKey.create(typeRegistry.key(), typeKey), paramSet));
    }

    @Override
    public DataComponentType<?> type() {
        return typeRegistry.getOrThrow(typeKey);
    }
}