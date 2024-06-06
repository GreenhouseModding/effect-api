
package dev.greenhouseteam.effectapi.api.effect;

import dev.greenhouseteam.effectapi.api.effect.entity.EffectAPIInstancedEffect;
import net.minecraft.world.level.storage.loot.LootContext;

public interface TickEffect<T extends EffectAPIInstancedEffect> extends EffectAPIEffect {

    T effect();

    default void tick(LootContext lootContext) {
        effect().apply(lootContext);
    }
}
