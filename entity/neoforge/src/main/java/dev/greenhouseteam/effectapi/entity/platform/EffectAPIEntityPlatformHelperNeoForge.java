package dev.greenhouseteam.effectapi.entity.platform;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.entity.api.entity.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.entity.impl.registry.EffectAPIAttachments;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EffectAPIEntityPlatformHelperNeoForge implements EffectAPIEntityPlatformHelper {
    @Override
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).orElse(null);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setData(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, ResourceLocation id, T value, @Nullable ResourceLocation source) {
        return entity.getData(EffectAPIAttachments.RESOURCES).setValue(id, value, source);
    }

    @Override
    public void removeResource(Entity entity, ResourceLocation id, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.resources().remove(id);
        if (attachment.resources().isEmpty())
            entity.removeData(EffectAPIAttachments.RESOURCES);
    }

    @Override
    public @Nullable EntityEffectsAttachment getEntityEffects(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.EFFECTS).orElse(null);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        entity.getData(EffectAPIAttachments.EFFECTS).addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        var attachment = entity.getExistingData(EffectAPIAttachments.EFFECTS);
        if (attachment.isPresent()) {
            attachment.get().removeEffect(effect, source);
            if (attachment.get().isEmpty())
                entity.removeData(EffectAPIAttachments.EFFECTS);
        }
    }

    @Override
    public void setEntityEffects(Entity entity, Map<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents) {
        if (alLComponents.isEmpty())
            entity.removeData(EffectAPIAttachments.EFFECTS);
        else
            entity.getData(EffectAPIAttachments.EFFECTS).setComponents(alLComponents, activeComponents);
    }
}