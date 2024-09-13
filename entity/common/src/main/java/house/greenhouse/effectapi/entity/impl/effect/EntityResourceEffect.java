package house.greenhouse.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.EntityResourceAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EntityResourceEffect<T> extends ResourceEffect<T> {
    public static final Codec<EntityResourceEffect<?>> CODEC = ResourceEffectCodec.create(triple -> new EntityResourceEffect<>(triple.getLeft(), triple.getMiddle(), triple.getRight()));

    public EntityResourceEffect(ResourceLocation id, Codec<T> resourceType,
                                T defaultValue) {
        super(id, resourceType, defaultValue);
    }

    @Override
    public void onAdded(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        EntityResourceAPI.setResourceValue(entity, id, EntityResourceAPI.hasResource(entity, id) ? EntityResourceAPI.getResourceValue(entity, id) : defaultValue, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        EntityResourceAPI.removeResource(entity, id, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
    }

    @Override
    public void onRefreshed(LootContext context) {}

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_RESOURCE;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
