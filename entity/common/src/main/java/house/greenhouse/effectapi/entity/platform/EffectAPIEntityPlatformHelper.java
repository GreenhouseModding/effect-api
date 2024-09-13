package house.greenhouse.effectapi.entity.platform;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EffectAPIEntityPlatformHelper {

    @Nullable
    ResourcesAttachment getResources(Entity entity);

    boolean hasResource(Entity entity, ResourceLocation id);

    void setResourcesAttachment(Entity entity, ResourcesAttachment attachment);

    <T> T setResource(Entity entity, ResourceLocation id, T value, ResourceLocation source);

    void removeResource(Entity entity, ResourceLocation id, ResourceLocation source);

    @Nullable
    EffectsAttachment<Entity> getEntityEffects(Entity entity);

    void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source);

    void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source);

    void setEntityEffects(Entity entity, Object2ObjectArrayMap<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents);
}