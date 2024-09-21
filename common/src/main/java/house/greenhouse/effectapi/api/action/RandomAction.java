package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.EffectAPICodecs;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;

public record RandomAction(SimpleWeightedRandomList<EffectAPIAction> actions) implements EffectAPIAction {
    public static MapCodec<RandomAction> staticCodec(LootContextParamSet paramSet) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                SimpleWeightedRandomList.wrappedCodecAllowingEmpty(EffectAPICodecs.validatedActionCodec(paramSet)).fieldOf("actions").forGetter(RandomAction::actions)
        ).apply(inst, RandomAction::new));
    }
    public static final ActionType<RandomAction> TYPE = new ActionType<>(RandomAction::staticCodec);

    public void apply(LootContext context) {
        actions().getRandomValue(context.getRandom()).ifPresent(action -> action.apply(context));
    }

    public Collection<LootContextParam<?>> requiredParams() {
        return actions().unwrap().stream().flatMap(action -> action.data().requiredParams().stream()).toList();
    }

    @Override
    public ActionType<?> type() {
        return TYPE;
    }
}
