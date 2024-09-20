
package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.function.Function;

public interface TickEffect<A extends EffectAPIAction> extends EffectAPIEffect {
    /**
     * Creates a tick effect codec with a specified action codec.
     * @param actionCodec   An action codec, typically a dispatch codec associated with a registry.
     * @param constructor   A constructor for the tick effect class.
     * @return              A tick effect codec.
     * @param <T>           The tick effect class.
     */
    static <A extends EffectAPIAction, T extends TickEffect<A>> Codec<T> codec(Codec<EffectAPIAction> actionCodec, Function<EffectAPIAction, T> constructor) {
        return actionCodec.xmap(constructor, TickEffect::action);
    }

    /**
     * The action to run upon this effect ticking.
     */
    A action();

    default void tick(LootContext lootContext) {
        action().apply(lootContext);
    }

    default boolean shouldTick(LootContext context, boolean isActive) {
        return isActive;
    }
}
