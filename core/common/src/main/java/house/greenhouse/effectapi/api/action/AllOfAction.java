package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface AllOfAction extends EffectAPIAction {
    static <T extends AllOfAction> MapCodec<T> createCodec(Codec<EffectAPIAction> actionCodec, Function<List<EffectAPIAction>, T> constructor) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                actionCodec.listOf().fieldOf("actions").forGetter(AllOfAction::actions)
        ).apply(inst, constructor));
    }

    default void apply(LootContext context) {
        actions().forEach(action -> action.apply(context));
    }

    default Collection<LootContextParam<?>> requiredParams() {
        return actions().stream().flatMap(action -> action.requiredParams().stream()).toList();
    }

    List<EffectAPIAction> actions();
}
