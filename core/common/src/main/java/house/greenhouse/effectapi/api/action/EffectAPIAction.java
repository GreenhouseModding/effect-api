package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

import java.util.Collection;

public interface EffectAPIAction {
    void apply(LootContext context);

    Collection<LootContextParam<?>> requiredParams();

    MapCodec<? extends EffectAPIAction> codec();
}
