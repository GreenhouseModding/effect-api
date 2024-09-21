package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;

/**
 * A generic action interface, for use in effects.
 * Actions are a configurable object that perform functions on a {@link LootContext}.
 * <br>
 * Actions must be registered to a respective registry in order to work.
 *
 * @see house.greenhouse.effectapi.api.effect.EffectAPIEffect
 * @see house.greenhouse.effectapi.api.EffectAPIActionTypes
 */
public interface EffectAPIAction {
    /**
     * The function to apply on the context.
     * @param context   The context.
     */
    void apply(LootContext context);

    /**
     * Any required parameters for this action to function.
     * @see LootContextParamSet#getRequired()
     */
    Collection<LootContextParam<?>> requiredParams();

    /**
     * The type of this action.
     */
    ActionType<?> type();
}
