package house.greenhouse.effectapi.entity.platform;

import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.entity.impl.registry.EffectAPIEntityAttachments;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EffectAPIEntityPlatformHelperFabric implements EffectAPIEntityPlatformHelper {

    @Override
    @Nullable
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getAttached(EffectAPIAttachments.RESOURCES);
    }

    @Override
    public boolean hasResource(Entity entity, ResourceLocation id) {
        return Optional.ofNullable(entity.getAttached(EffectAPIAttachments.RESOURCES)).map(attachment -> attachment.hasResource(id)).orElse(false);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setAttached(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, ResourceLocation id, T value, ResourceLocation source) {
        ResourcesAttachment attachment = entity.getAttachedOrCreate(EffectAPIAttachments.RESOURCES);
        return attachment.setValue(id, value, source);
    }

    @Override
    public void removeResource(Entity entity, ResourceLocation id, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.removeValue(id);
        if (attachment.resources().isEmpty())
            entity.removeAttached(EffectAPIAttachments.RESOURCES);
    }


    @Override
    public @Nullable EffectsAttachment getEntityEffects(Entity entity) {
        return entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        if (entity.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS) && entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).hasEffect(effect, true))
            return;
        EffectsAttachment attachment =  entity.getAttachedOrCreate(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        attachment.init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        EffectsAttachment attachment =  entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        if (attachment == null || !attachment.hasEffect(effect, true))
            return;
        if (attachment.isActive(effect))
            effect.onRemoved(EntityEffectAPI.createEntityOnlyContext(entity));
        attachment.removeEffect(effect, source);
        if (attachment.isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, Object2ObjectArrayMap<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents) {
        if (alLComponents.isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        else
            entity.getAttachedOrCreate(EffectAPIEntityAttachments.ENTITY_EFFECTS).setComponents(alLComponents, activeComponents);
    }
}
