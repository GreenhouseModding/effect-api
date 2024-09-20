package house.greenhouse.effectapi.api;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

/**
 * This class provides some common hooks for the entity effects attachment.
 * <br>
 * The attachment is a way to have entity effects work across different
 * modded implementations, so you don't have to struggle with compatibility with one
 * mod's effects that is only handled through its own code.
 */
public class EntityEffectAPI {
    /**
     * Obtains an entity's active effects.
     *
     * @param entity            The entity to get the effects of.
     * @param type              The type of effect.
     * @return                  A list of effects.
     * @param <E>               The effect class.
     */
    public static <E extends EffectAPIEffect> List<E> getEffects(Entity entity, EffectType<E> type) {
        return getEffects(entity, type, false);
    }

    /**
     * Obtains an entity's effects.
     *
     * @param entity            The entity to get the effects of.
     * @param type              The type of effect.
     * @param includeInactive   Whether to include inactive effects.
     * @return                  A list of effects.
     * @param <E>               The effect class.
     */
    public static <E extends EffectAPIEffect> List<E> getEffects(Entity entity, EffectType<E> type, boolean includeInactive) {
        EffectsAttachment attachment = EffectAPI.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return List.of();
        return EffectAPI.getHelper().getEntityEffects(entity).getEffects(type, includeInactive);
    }

    /**
     * Checks if an entity has a specific effect type active.
     * For checking specific effects, or checking if more than one is active, use {@link EntityEffectAPI#getEffects(Entity, EffectType)}
     *
     * @param entity    The entity to check.
     * @param type      The type of effect to check.
     * @return          True if the entity has an instance of the effect, false if not.
     */
    public static <E extends EffectAPIEffect> boolean isTypeActive(Entity entity, EffectType<E> type) {
        return hasType(entity, type, false);
    }


    /**
     * Checks if an entity has a specific effect type.
     * For checking specific effects, use {@link EntityEffectAPI#getEffects(Entity, EffectType)}
     *
     * @param entity    The entity to check.
     * @param type      The type of effect to check.
     * @return          True if the entity has an instance of the effect, false if not.
     */
    public static <E extends EffectAPIEffect> boolean hasType(Entity entity, EffectType<E> type, boolean includeInactive) {
        EffectsAttachment attachment = EffectAPI.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return false;
        return attachment.hasEffectType(type, includeInactive);
    }

    /**
     * Adds an effect to an entity.
     * This does not always mean that it is active (see: {@link EffectAPIConditionalEffect})
     *
     * @param entity    The entity to add the effect to.
     * @param effect    The effect to add.
     * @param source    The source of the effect.
     */
    public static void addEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        EffectAPI.getHelper().addEntityEffect(entity, effect, source);
    }

    /**
     * Adds multiple effects to an entity under the same source.
     *
     * @param entity    The entity to add the effects to.
     * @param effects   The effects to add.
     * @param source    The source of the effects.
     */
    public static void addEffects(Entity entity, List<? extends EffectHolder<EffectAPIEffect>> effects, ResourceLocation source) {
        for (EffectHolder<EffectAPIEffect> effect : effects) {
            EffectAPI.getHelper().addEntityEffect(entity, effect, source);
        }
    }

    /**
     * Removes an effect from an entity.
     *
     * @param entity    The entity to remove the effect from.
     * @param effect    The effect to remove.
     * @param source    The source of the effect.
     */
    public static void removeEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        EffectAPI.getHelper().removeEntityEffect(entity, effect, source);
    }

    /**
     * Removes effects from an entity.
     *
     * @param entity    The entity to remove the effects from.
     * @param effects   The effects to remove.
     * @param source    The source of the effect.
     */
    public static void removeEffects(Entity entity, List<? extends EffectHolder<EffectAPIEffect>> effects, ResourceLocation source) {
        for (EffectHolder<EffectAPIEffect> effect : effects) {
            EffectAPI.getHelper().removeEntityEffect(entity, effect, source);
        }
    }

    /**
     * Syncs an entity's effects with any players surrounding it.
     *
     * @param entity    The entity to sync.
     */
    public static void syncEffects(Entity entity) {
        EffectsAttachment attachment = EffectAPI.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return;
        attachment.sync();
    }
}
