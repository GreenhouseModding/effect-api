package house.greenhouse.effectapi.entity.impl.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.EntityResourceAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class EntityResourceEffect<T> extends ResourceEffect<T> {
    public static final Codec<EntityResourceEffect<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Resource.CODEC.fieldOf("resource").forGetter(effect -> (Holder<Resource<?>>)(Holder<?>)effect.resource)
    ).apply(inst, resourceHolder -> new EntityResourceEffect(resourceHolder)));

    public EntityResourceEffect(Holder<Resource<T>> resource) {
        super(resource);
    }

    @Override
    public void onAdded(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        EntityResourceAPI.addResourceValue(entity, resource, EntityResourceAPI.hasResource(entity, resource) ? EntityResourceAPI.getResourceValue(entity, resource) : resource.value().defaultValue(), lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        EntityResourceAPI.removeResource(entity, resource, lootContext.getParamOrNull(EffectAPILootContextParams.SOURCE));
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
