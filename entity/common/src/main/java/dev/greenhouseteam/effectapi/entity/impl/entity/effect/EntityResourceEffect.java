package dev.greenhouseteam.effectapi.entity.impl.entity.effect;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParams;
import dev.greenhouseteam.effectapi.entity.api.entity.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.entity.api.entity.registry.EffectAPIEntityLootContextParamSets;
import dev.greenhouseteam.effectapi.entity.impl.entity.EffectAPIEntity;
import dev.greenhouseteam.effectapi.entity.impl.entity.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public class EntityResourceEffect<T> extends ResourceEffect<T> {
    public static final Codec<EntityResourceEffect<?>> CODEC = ResourceEffectCodec.create(triple -> new EntityResourceEffect<>(triple.getLeft(), triple.getMiddle(), triple.getRight()));

    public EntityResourceEffect(ResourceLocation id, Codec<T> resourceType,
                                T defaultValue) {
        super(id, resourceType, defaultValue);
    }

    @Override
    public void onAdded(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        T value = EffectAPIEntity.getHelper().setResource(entity, id, defaultValue, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
        EffectAPI.getHelper().sendClientboundTracking(new ChangeResourceClientboundPacket<>(entity.getId(), this, Optional.ofNullable(lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE)), Optional.of(value)), entity);
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        EffectAPIEntity.getHelper().removeResource(entity, id, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
        EffectAPI.getHelper().sendClientboundTracking(new ChangeResourceClientboundPacket<>(entity.getId(), this, Optional.empty(), Optional.empty()), entity);
    }

    @Override
    public void onRefreshed(LootContext context) {}

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.RESOURCE;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
