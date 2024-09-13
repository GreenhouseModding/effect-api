package house.greenhouse.effectapi.entity.api;

import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.entity.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
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
     * @param resourceId    The resource id to check for.
     * @return              True if the entity has the resource, no matter the value, false if not.
     */
    public static boolean hasResource(Entity entity, ResourceLocation resourceId) {
        ResourcesAttachment attachment = EffectAPIEntity.getHelper().getResources(entity);
        if (attachment == null)
            return false;
        return attachment.hasResource(resourceId);
    }

    /**
     * Gets a resource's value on an entity.
     *
     * @param entity        The entity to get the specified resource from.
     * @param resourceId    The id of the resource.
     * @return              The resource's value.
     * @param <T>           The type of the resource's value.
     */
    @Nullable
    public static <T> T getResourceValue(Entity entity, ResourceLocation resourceId) {
        ResourcesAttachment attachment = EffectAPIEntity.getHelper().getResources(entity);
        if (attachment == null)
            return null;
        return attachment.getValue(resourceId);
    }

    /**
     * Sets a resource on an entity, giving them the resource if they do not already have it..
     *
     * @param entity    The entity to add the effect to.
     * @param source    The source of the effect.
     */
    public static <T> T setResourceValue(Entity entity, ResourceLocation resourceId, T value, ResourceLocation source) {
        EffectAPIEntity.getHelper().setResource(entity, resourceId, value, source);
        if (!entity.level().isClientSide())
            EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(entity.getId(), EffectAPIEntity.getHelper().getResources(entity)), entity);
        return value;
    }

    /**
     * Removes a resource from an entity.
     *
     * @param entity        The entity to remove the resource from.
     * @param resourceId    The id of the resource to remove.
     * @param source        The source of the resource.
     */
    public static void removeResource(Entity entity, ResourceLocation resourceId, ResourceLocation source) {
        EffectAPIEntity.getHelper().removeResource(entity, resourceId, source);
        if (EffectAPIEntity.getHelper().getResources(entity) != null && !entity.level().isClientSide())
            EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(entity.getId(), EffectAPIEntity.getHelper().getResources(entity)), entity);
    }
}
