package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

/**
 * A Dummy Effect.
 * Typically used as a placeholder for when developers need to check if a specific data structure is present, but you don't want any operations.
 * <br>
 * Mod developers are discouraged from hardcoding their individual effects using this class. Only use this if you really do not need any fields.
 *
 * @param typeRegistry  The registry this type is registered to.
 * @param typeKey       The {@link ResourceLocation} of this type.
 */
public record DummyEffect(Registry<EffectType<?>> typeRegistry, ResourceKey<EffectType<?>> typeKey) implements EffectAPIEffect {
    public static Codec<DummyEffect> codec(Registry<EffectType<?>> typeRegistry, ResourceLocation typeKey) {
        return Codec.unit(() -> new DummyEffect(typeRegistry, ResourceKey.create(typeRegistry.key(), typeKey)));
    }

    @Override
    public EffectType<?> type() {
        return typeRegistry.getOrThrow(typeKey);
    }
}