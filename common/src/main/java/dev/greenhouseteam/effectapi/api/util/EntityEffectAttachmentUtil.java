package dev.greenhouseteam.effectapi.api.util;

import dev.greenhouseteam.effectapi.api.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class EntityEffectAttachmentUtil {
    /**
     * Obtains an entity's active effects.
     *
     * @param entity    The entity to get the effects of.
     * @param type      The type of effect.
     * @return          A list of effects.
     * @param <T>       The effect class.
     */
    public static <T extends EffectAPIEffect> List<T> getEntityEffects(Entity entity, DataComponentType<List<T>> type) {
        EntityEffectsAttachment attachment = EffectAPI.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return List.of();
        return EffectAPI.getHelper().getEntityEffects(entity).getEffects(type);
    }

    public static boolean hasEffectType(Entity entity, DataComponentType<?> type) {
        EntityEffectsAttachment attachment = EffectAPI.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return false;
        return !attachment.getEffects((DataComponentType)type).isEmpty();
    }

    /**
     * Adds an effect to an entity.
     * This does not always mean that it is active (see: {@link dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect})
     *
     * @param entity    The entity to add the effect to.
     * @param effect    The effect to add.
     * @param source    The source of the effect.
     */
    public static void addEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        EffectAPI.getHelper().addEntityEffect(entity, effect, source);
    }

    /**
     * Adds multiple effects to an entity under the same source.
     *
     * @param entity    The entity to add the effects to.
     * @param effects   The effects to add.
     * @param source    The source of the effects.
     */
    public static void addEffects(Entity entity, List<? extends EffectAPIEffect> effects, ResourceLocation source) {
        for (EffectAPIEffect effect : effects)
            EffectAPI.getHelper().addEntityEffect(entity, effect, source);
    }

    /**
     * Removes an effect from an entity.
     *
     * @param entity    The entity to remove the effect from.
     * @param effect    The effect to remove.
     * @param source    The source of the effect.
     */
    public static void removeEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        EffectAPI.getHelper().removeEntityEffect(entity, effect, source);
    }

    /**
     * Removes effects from an entity.
     *
     * @param entity    The entity to remove the effects from.
     * @param effects   The effects to remove.
     * @param source    The source of the effect.
     */
    public static void removeEffects(Entity entity, List<? extends EffectAPIEffect> effects, ResourceLocation source) {
        for (EffectAPIEffect effect : effects)
            EffectAPI.getHelper().removeEntityEffect(entity, effect, source);
    }

    /**
     * Syncs an entity's effects with any clients surrounding it.
     *
     * @param entity    The entity to sync.
     */
    public static void syncEffects(Entity entity) {
        EffectAPI.getHelper().getEntityEffects(entity).sync();
    }
}
