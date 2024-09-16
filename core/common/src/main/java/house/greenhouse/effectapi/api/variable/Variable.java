package house.greenhouse.effectapi.api.variable;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.Collection;

public interface Variable<T> {
    T get(LootContext context);

    DataType<T> dataType();

    /**
     * Any required parameters for this effect to function.
     * @see LootContextParamSet#getRequired()
     */
    Collection<LootContextParam<?>> requiredParams();

    MapCodec<? extends Variable<?>> codec();
}
