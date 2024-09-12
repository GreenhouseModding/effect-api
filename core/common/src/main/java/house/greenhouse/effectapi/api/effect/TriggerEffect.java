package house.greenhouse.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.action.EffectAPIAction;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;
import java.util.function.BiFunction;

public interface TriggerEffect extends EffectAPIEffect {
    static <T extends TriggerEffect> Codec<T> createCodec(Codec<EffectAPIAction> actionCodec, Constructor<T> constructor) {
        return RecordCodecBuilder.create(inst -> inst.group(
                actionCodec.optionalFieldOf("on_added").forGetter(TriggerEffect::onAdded),
                actionCodec.optionalFieldOf("on_removed").forGetter(TriggerEffect::onRemoved),
                actionCodec.optionalFieldOf("on_refresh").forGetter(TriggerEffect::onRefresh)
        ).apply(inst, constructor::apply));
    }

    @Override
    default void onAdded(LootContext context) {
        onAdded().ifPresent(effect -> effect.apply(context));
    }

    @Override
    default void onRemoved(LootContext context) {
        onRemoved().ifPresent(effect -> effect.apply(context));
    }

    @Override
    default void onRefreshed(LootContext context) {
        onRefresh().ifPresent(effect -> effect.apply(context));
    }

    Optional<EffectAPIAction> onAdded();

    Optional<EffectAPIAction> onRemoved();

    Optional<EffectAPIAction> onRefresh();

    @FunctionalInterface
    interface Constructor<T> {
        T apply(Optional<EffectAPIAction> onAdded, Optional<EffectAPIAction> onRemoved, Optional<EffectAPIAction> onRefreshed);
    }
}
