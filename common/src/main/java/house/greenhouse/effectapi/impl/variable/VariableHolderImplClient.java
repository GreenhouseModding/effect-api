package house.greenhouse.effectapi.impl.variable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.effectapi.api.variable.JsonReference;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;
import java.util.Map;

public class VariableHolderImplClient<T> extends VariableHolderImpl<T> {
    public VariableHolderImplClient(Codec<T> codec, Map<List<JsonReference>, Variable<?>> variableMap, JsonElement rawJson) {
        super(codec, variableMap, rawJson);
    }

    @Override
    public T construct(LootContext context, Map<List<JsonReference>, Object> variableValues) {
        throw new UnsupportedOperationException("You should not be calling VariableHolder#getOrCreate on the client!");
    }

    @Override
    public <TOps> DataResult<VariableHolder<T>> validate(DynamicOps<TOps> ops) {
        // Validation was already handled on the server, sooo...
        return DataResult.success(this);
    }
}
