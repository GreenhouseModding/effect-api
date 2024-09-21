package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.api.EffectAPICodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;
import java.util.List;

public record AllOfAction(List<EffectAPIAction> actions) implements EffectAPIAction {
    public static MapCodec<AllOfAction> staticCodec(LootContextParamSet paramSet) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                EffectAPICodecs.validatedActionCodec(paramSet).listOf().fieldOf("actions").forGetter(AllOfAction::actions)
        ).apply(inst, AllOfAction::new));
    }
    public static final ActionType<AllOfAction> TYPE = new ActionType<>(AllOfAction::staticCodec);

    public void apply(LootContext context) {
        actions().forEach(action -> action.apply(context));
    }

    public Collection<LootContextParam<?>> requiredParams() {
        return actions().stream().flatMap(action -> action.requiredParams().stream()).toList();
    }

    @Override
    public ActionType<?> type() {
        return TYPE;
    }
}
