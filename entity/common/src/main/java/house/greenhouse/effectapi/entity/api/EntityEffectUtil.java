package house.greenhouse.effectapi.entity.api;

import house.greenhouse.effectapi.api.effect.EffectAPIConditionalEffect;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import house.greenhouse.effectapi.entity.api.attachment.EntityEffectsAttachment;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * This class provides some common hooks for the entity effects attachment.
 * <br>
 * The attachment is a way to have entity effects work across different
 * modded implementations, so you don't have to struggle with compatibility with one
 * mod's effects that is only handled through its own code.
 */
public class EntityEffectUtil {
    /**
     * Obtains an entity's active effects.
     *
     * @param entity    The entity to get the effects of.
     * @param type      The type of effect.
     * @return          A list of effects.
     * @param <T>       The effect class.
     */
    public static <T extends EffectAPIEffect> List<T> getEntityEffects(Entity entity, DataComponentType<List<T>> type) {
        EntityEffectsAttachment attachment = EffectAPIEntity.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return List.of();
        return EffectAPIEntity.getHelper().getEntityEffects(entity).getEffects(type);
    }

    /**
     * Checks if an entity has a specific effect type active.
     * For checking specific effects, or checking if multiple are active, use {@link EntityEffectUtil#getEntityEffects(Entity, DataComponentType)}
     *
     * @param entity    The entity to check.
     * @param type      The type of effect to check.
     * @return          True if the entity has an instance of the effect, false if not.
     */
    public static boolean hasEffectType(Entity entity, DataComponentType<?> type) {
        EntityEffectsAttachment attachment = EffectAPIEntity.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return false;
        return !attachment.getEffects((DataComponentType)type).isEmpty();
    }

    /**
     * Adds an effect to an entity.
     * This does not always mean that it is active (see: {@link EffectAPIConditionalEffect})
     *
     * @param entity    The entity to add the effect to.
     * @param effect    The effect to add.
     * @param source    The source of the effect.
     */
    public static void addEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        effect.onRemoved(createEntityOnlyContext(entity, source));
        EffectAPIEntity.getHelper().addEntityEffect(entity, effect, source);
    }

    /**
     * Adds multiple effects to an entity under the same source.
     *
     * @param entity    The entity to add the effects to.
     * @param effects   The effects to add.
     * @param source    The source of the effects.
     */
    public static void addEffects(Entity entity, List<? extends EffectAPIEffect> effects, ResourceLocation source) {
        for (EffectAPIEffect effect : effects) {
            effect.onRemoved(createEntityOnlyContext(entity, source));
            EffectAPIEntity.getHelper().addEntityEffect(entity, effect, source);
        }
    }

    /**
     * Removes an effect from an entity.
     *
     * @param entity    The entity to remove the effect from.
     * @param effect    The effect to remove.
     * @param source    The source of the effect.
     */
    public static void removeEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        effect.onRemoved(createEntityOnlyContext(entity, source));
        EffectAPIEntity.getHelper().removeEntityEffect(entity, effect, source);
    }

    /**
     * Removes effects from an entity.
     *
     * @param entity    The entity to remove the effects from.
     * @param effects   The effects to remove.
     * @param source    The source of the effect.
     */
    public static void removeEffects(Entity entity, List<? extends EffectAPIEffect> effects, ResourceLocation source) {
        for (EffectAPIEffect effect : effects) {
            effect.onRemoved(createEntityOnlyContext(entity, source));
            EffectAPIEntity.getHelper().removeEntityEffect(entity, effect, source);
        }
    }

    /**
     * Syncs an entity's effects with any players surrounding it.
     *
     * @param entity    The entity to sync.
     */
    public static void syncEffects(Entity entity) {
        EntityEffectsAttachment attachment = EffectAPIEntity.getHelper().getEntityEffects(entity);
        if (attachment == null)
            return;
        attachment.sync();
    }

    /**
     * Creates a {@link LootContext} based on the entity provided, without an effect source.
     * Please use {@link EntityEffectUtil#createEntityOnlyContext(Entity, ResourceLocation)} in contexts where you need an effect source.
     *
     * @param entity    The provided entity.
     * @return          A {@link LootContext} with the entity's position and the entity.
     */
    public static LootContext createEntityOnlyContext(Entity entity) {
        return createEntityOnlyContext(entity, null);
    }

    /**
     *`Creates a {@link LootContext} based on the entity provided, with an effect source.
     *
     * @param entity    The provided entity.
     * @param source    An effect source, passed as {@link EffectAPILootContextParams#SOURCE}.
     * @return          A {@link LootContext} with the entity's position, the entity, and the effect source.
     */
    public static LootContext createEntityOnlyContext(Entity entity, @Nullable ResourceLocation source) {
        if (entity.level().isClientSide())
            return null;
        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        params.withOptionalParameter(EffectAPILootContextParams.SOURCE, source);
        return new LootContext.Builder(params.create(EffectAPIEntityLootContextParamSets.ENTITY)).create(Optional.empty());
    }
}
