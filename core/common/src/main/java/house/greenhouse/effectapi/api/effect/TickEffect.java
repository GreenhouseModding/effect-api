
package house.greenhouse.effectapi.api.effect;

import house.greenhouse.effectapi.api.effect.instanced.EffectAPIInstancedEffect;
import net.minecraft.world.level.storage.loot.LootContext;

public interface TickEffect<T extends EffectAPIInstancedEffect> extends EffectAPIEffect {

    T effect();

    default void tick(LootContext lootContext) {
        effect().apply(lootContext);
    }
}
