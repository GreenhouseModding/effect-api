package house.greenhouse.effectapi.api.effect;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

/**
 * A generic effect interface.
 * Effects are an object that can be applied to {@link LootContext} params to create new behavior that is not present by default.
 * <br>
 * Effects are typically stored in an attachment, however they do not (de)serialize, you should typically create an attachment of your own if you want effects to persist.
 * You are free to read the entity module's testdata for an example of how to set up an attachment.
 * Effects persist on respawn, if you need to remove effects upon respawn, you will have to handle that yourself.
 * <br>
 * Effects must be registered to a respective registry in order to work.
 *
 * @see house.greenhouse.effectapi.api.EffectAPIEffectTypes
 * @see LootContext
 */
public interface EffectAPIEffect {
    /**
     * Runs whenever an effect turns from inactive to active within an attachment.
     * @param context   The context of the effect within the attachment.
     */
    default void onAdded(LootContext context) {}

    /**
     * Runs whenever an effect turns from active to inactive within an attachment.
     * @param context   The context of the effect within the attachment.
     */
    default void onRemoved(LootContext context) {}

    /**
     * Runs whenever an effect is refreshed on an object.
     * Some examples of when an effect should be refreshed are an entity joining a level, and on entity respawn.
     * @param context   The context of the effect within the attachment.
     */
    default void onRefreshed(LootContext context) {
        onRemoved(context);
    }

    /**
     * Runs whenever an effect should tick.
     * @param context   The context of the effect within the attachment.
     */
    default void tick(LootContext context) {}

    /**
     * Whether this effect should tick.
     * @param context   The context of the effect within the attachment.
     * @param isActive  Whether this effect is active. This field exists to avoid double up conditional checks.
     */
    default boolean shouldTick(LootContext context, boolean isActive) {
        return false;
    }

    /**
     * Whether this effect is active.
     * @param context   The context of the effect within the attachment.
     */
    default boolean isActive(LootContext context) {
        return true;
    }

    /**
     * The type of this effect.
     */
    DataComponentType<?> type();

    /**
     * The param set of this effect, used for validation.
     */
    LootContextParamSet paramSet();
}
