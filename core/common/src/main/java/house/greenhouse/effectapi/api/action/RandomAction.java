package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Collection;
import java.util.function.Function;

public interface RandomAction extends EffectAPIAction {
    static <T extends RandomAction> MapCodec<T> createCodec(Codec<EffectAPIAction> actionCodec, Function<SimpleWeightedRandomList<EffectAPIAction>, T> constructor) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                SimpleWeightedRandomList.wrappedCodecAllowingEmpty(actionCodec).fieldOf("actions").forGetter(RandomAction::actions)
        ).apply(inst, constructor));
    }

    default void apply(LootContext context) {
        actions().getRandomValue(context.getRandom()).ifPresent(action -> action.apply(context));
    }

    default Collection<LootContextParam<?>> requiredParams() {
        return actions().unwrap().stream().flatMap(action -> action.data().requiredParams().stream()).toList();
    }

    SimpleWeightedRandomList<EffectAPIAction> actions();
}
