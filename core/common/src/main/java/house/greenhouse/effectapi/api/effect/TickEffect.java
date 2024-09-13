
package house.greenhouse.effectapi.api.effect;

import house.greenhouse.effectapi.api.action.EffectAPIAction;
import net.minecraft.world.level.storage.loot.LootContext;

public interface TickEffect<T extends EffectAPIAction> extends EffectAPIEffect {

    T effect();

    default void tick(LootContext lootContext) {
        effect().apply(lootContext);
    }

    default boolean shouldTick(LootContext context, boolean isActive) {
        return isActive;
    }
}
