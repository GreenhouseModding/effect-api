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

public class EffectAPIEntityPlatformHelperNeoForge implements EffectAPIEntityPlatformHelper {
    @Override
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).orElse(null);
    }

    @Override
    public boolean hasResource(Entity entity, ResourceLocation id) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).map(attachment -> attachment.hasResource(id)).orElse(false);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setData(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, ResourceLocation id, T value, @Nullable ResourceLocation source) {
        ResourcesAttachment attachment = entity.getData(EffectAPIAttachments.RESOURCES);
        return attachment.setValue(id, value, source);
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
    public @Nullable EffectsAttachment<Entity> getEntityEffects(Entity entity) {
        return entity.getExistingData(EffectAPIEntityAttachments.ENTITY_EFFECTS).orElse(null);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        if (entity.hasData(EffectAPIEntityAttachments.ENTITY_EFFECTS) && entity.getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).hasEffect(effect, true))
            return;
        EffectsAttachment<Entity> attachment = entity.getData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        attachment.init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        var attachment = entity.getExistingData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        if (attachment.isEmpty() || !attachment.get().hasEffect(effect, true))
            return;
        if (attachment.get().isActive(effect))
            effect.onRemoved(EntityEffectAPI.createEntityOnlyContext(entity));
        attachment.get().removeEffect(effect, source);
        if (attachment.get().isEmpty())
            entity.removeData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, Object2ObjectArrayMap<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents) {
        if (alLComponents.isEmpty())
            entity.removeData(EffectAPIEntityAttachments.ENTITY_EFFECTS);
        else
            entity.getData(EffectAPIEntityAttachments.ENTITY_EFFECTS).setComponents(alLComponents, activeComponents);
    }
}