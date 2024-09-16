package house.greenhouse.effectapi.entity.platform;

import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.EffectHolder;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.entity.impl.registry.EffectAPIEntityAttachments;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class EffectAPIEntityPlatformHelperNeoForge implements EffectAPIEntityPlatformHelper {
    @Override
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).orElse(null);
    }

    @Override
    public <T> boolean hasResource(Entity entity, Holder<Resource<T>> resource) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).map(attachment -> attachment.hasResource(resource)).orElse(false);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setData(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, Holder<Resource<T>> resource, T value, @Nullable ResourceLocation source) {
        if (source == null && !entity.hasData(EffectAPIAttachments.RESOURCES))
            return null;
        ResourcesAttachment attachment = entity.getData(EffectAPIAttachments.RESOURCES);
        return attachment.setValue(resource, value, source);
    }

    @Override
    public <T> void removeResource(Entity entity, Holder<Resource<T>> resource, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.removeValue(resource, source);
        if (attachment.isEmpty())
            entity.removeData(EffectAPIAttachments.RESOURCES);
    }

    @Override
    public @Nullable EffectsAttachmentImpl<Entity> getEntityEffects(Entity entity) {
        return entity.getExistingData(EffectAPIEntityAttachments.ENTITY_EFFECTS).orElse(null);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (entity.hasData(EffectAPIEntityAttachments.ENTITY_EFFECTS) && entity.getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).hasEffect(effect, true))
            return;
        EffectsAttachmentImpl<Entity> attachment = entity.getData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        attachment.init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        var attachment = entity.getExistingData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        if (attachment.isEmpty() || !attachment.get().hasEffect(effect, true))
            return;
        attachment.get().removeEffect(effect, source);
        if (attachment.get().isEmpty())
            entity.removeData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, DataComponentMap combinedComponents, DataComponentMap activeComponents) {
        if (combinedComponents.isEmpty())
            entity.removeData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        else
            entity.getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).setComponents(combinedComponents, activeComponents);
    }
}