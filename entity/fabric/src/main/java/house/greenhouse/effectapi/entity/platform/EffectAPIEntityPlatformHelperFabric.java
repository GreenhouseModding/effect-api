package house.greenhouse.effectapi.entity.platform;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.api.variable.EffectHolder;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.entity.impl.registry.EffectAPIEntityAttachments;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.core.Holder;
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
    public <T> boolean hasResource(Entity entity, Holder<Resource<T>> resource) {
        return Optional.ofNullable(entity.getAttached(EffectAPIAttachments.RESOURCES)).map(attachment -> attachment.hasResource(resource)).orElse(false);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setAttached(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, Holder<Resource<T>> resource, T value, ResourceLocation source) {
        if (source == null && !entity.hasAttached(EffectAPIAttachments.RESOURCES))
            return null;
        ResourcesAttachment attachment = entity.getAttachedOrCreate(EffectAPIAttachments.RESOURCES);
        return attachment.setValue(resource, value, source);
    }

    @Override
    public <T> void removeResource(Entity entity, Holder<Resource<T>> resource, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.removeValue(resource, source);
        if (attachment.isEmpty())
            entity.removeAttached(EffectAPIAttachments.RESOURCES);
    }


    @Override
    public @Nullable EffectsAttachment<Entity> getEntityEffects(Entity entity) {
        return entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (entity.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS) && entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).hasEffect(effect, true))
            return;
        EffectsAttachment<Entity> attachment =  entity.getAttachedOrCreate(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        ((EffectsAttachmentImpl<Entity>)attachment).init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        EffectsAttachment<Entity> attachment =  entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        if (attachment == null || !attachment.hasEffect(effect, true))
            return;
        attachment.removeEffect(effect, source);
        if (attachment.isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, DataComponentMap combinedComponents, DataComponentMap activeComponents) {
        if (combinedComponents.isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        else
            entity.getAttachedOrCreate(EffectAPIEntityAttachments.ENTITY_EFFECTS).setComponents(combinedComponents, activeComponents);
    }
}
