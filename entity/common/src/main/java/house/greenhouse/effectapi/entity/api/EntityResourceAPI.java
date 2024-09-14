package house.greenhouse.effectapi.entity.api;

import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.entity.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This class provides some common hooks for the resources attachment.
 */
public class EntityResourceAPI {
    /**
     * Checks if an entity has a specific resource.
     * This does not check the value, only if the resource is present on the entity.
     *
     * @param entity        The entity to check.
     * @param resource      The resource to check if the entity has.
     * @return              True if the entity has the resource, no matter the value, false if not.
     */
    public static <T> boolean hasResource(Entity entity, Holder<Resource<T>> resource) {
        ResourcesAttachment attachment = EffectAPIEntity.getHelper().getResources(entity);
        if (attachment == null)
            return false;
        return attachment.hasResource(resource);
    }

    /**
     * Gets a resource's value on an entity.
     *
     * @param entity        The entity to get the specified resource from.
     * @param resource      The resource to get the entity's value from.
     * @return              The resource's value.
     * @param <T>           The type of the resource's value.
     */
    @Nullable
    public static <T> T getResourceValue(Entity entity, Holder<Resource<T>> resource) {
        ResourcesAttachment attachment = EffectAPIEntity.getHelper().getResources(entity);
        if (attachment == null)
            return null;
        return attachment.getValue(resource);
    }

    /**
     * Sets a resource value on an entity if they have it.
     *
     * @param entity    The entity to add the effect to.
     * @param resource  The resource to set on the entity.
     * @param value     The value to set the resource to.
     */
    public static <T> T setResourceValue(Entity entity, Holder<Resource<T>> resource, T value) {
        EffectAPIEntity.getHelper().setResource(entity, resource, value, null);
        if (!entity.level().isClientSide())
            EffectAPI.getHelper().sendClientboundTracking(new ChangeEntityResourceClientboundPacket<>(entity.getId(), resource, Optional.empty(), Optional.of(value)), entity);
        return value;
    }

    /**
     * Sets a resource value on an entity, giving them the resource if they do not already have it.
     *
     * @param entity    The entity to add the effect to.
     * @param resource  The resource to set on the entity.
     * @param value     The value to set the resource to.
     * @param source    The source of the effect.
     */
    public static <T> T addResourceValue(Entity entity, Holder<Resource<T>> resource, T value, ResourceLocation source) {
        EffectAPIEntity.getHelper().setResource(entity, resource, value, source);
        if (!entity.level().isClientSide())
            EffectAPI.getHelper().sendClientboundTracking(new ChangeEntityResourceClientboundPacket<>(entity.getId(), resource, Optional.of(source), Optional.of(value)), entity);
        return value;
    }

    /**
     * Removes a resource from an entity.
     *
     * @param entity        The entity to remove the resource from.
     * @param resource      The resource to remove from the entity.
     * @param source        The source of the resource.
     */
    public static <T> void removeResource(Entity entity, Holder<Resource<T>> resource, ResourceLocation source) {
        EffectAPIEntity.getHelper().removeResource(entity, resource, source);
        if (EffectAPIEntity.getHelper().getResources(entity) != null && !entity.level().isClientSide())
            EffectAPI.getHelper().sendClientboundTracking(new ChangeEntityResourceClientboundPacket<>(entity.getId(), resource, Optional.of(source), Optional.empty()), entity);
    }
}
