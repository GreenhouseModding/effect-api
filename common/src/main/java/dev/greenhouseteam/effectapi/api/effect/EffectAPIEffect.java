package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.params.EffectAPILootContextParamSets;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public interface EffectAPIEffect {
    Codec<DataComponentType<?>> COMPONENT_CODEC = Codec.lazyInitialized(EffectAPIRegistries.EFFECT::byNameCodec);
    Codec<DataComponentMap> CODEC = DataComponentMap.makeCodec(COMPONENT_CODEC);

    DataComponentType<?> type();


    default void onAdded(LootContext lootContext) {}

    default void onRemoved(LootContext lootContext) {}

    default boolean isActive(LootContext lootContext) {
        return true;
    }

    LootContextParamSet paramSet();

    static LootContext createEntityOnlyContext(Entity entity) {
        if (entity.level().isClientSide())
            return null;
        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        return new LootContext.Builder(params.create(EffectAPILootContextParamSets.ENTITY)).create(Optional.empty());
    }
}
