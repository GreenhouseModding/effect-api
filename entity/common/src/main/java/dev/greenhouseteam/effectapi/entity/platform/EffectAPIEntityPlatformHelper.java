package dev.greenhouseteam.effectapi.entity.platform;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.entity.api.entity.attachment.EntityEffectsAttachment;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface EffectAPIEntityPlatformHelper {

    @Nullable
    ResourcesAttachment getResources(Entity entity);

    void setResourcesAttachment(Entity entity, ResourcesAttachment attachment);

    <T> T setResource(Entity entity, ResourceLocation id, T value, @Nullable ResourceLocation source);

    void removeResource(Entity entity, ResourceLocation id, ResourceLocation source);

    @Nullable
    EntityEffectsAttachment getEntityEffects(Entity entity);

    void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source);

    void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source);

    void setEntityEffects(Entity entity, Map<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents);
}