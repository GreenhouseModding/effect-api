package house.greenhouse.effectapi.impl.variable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class VariableCodecWrapper<T> implements Codec<VariableHolder<T>> {
    private final Codec<T> codec;
    private final Codec<Variable<?>> variableCodec;
    private final Constructor<T> constructor;

    public VariableCodecWrapper(Codec<T> codec, Codec<Variable<?>> variableCodec, Constructor<T> constructor) {
        this.codec = codec;
        this.variableCodec = variableCodec;
        this.constructor = constructor;
    }

    @Override
    public <TOps> DataResult<Pair<VariableHolder<T>, TOps>> decode(DynamicOps<TOps> ops, TOps input) {
        Map<String, Variable<?>> variableMap = new HashMap<>();
        JsonElement rawJson = ops.convertTo(JsonOps.INSTANCE, input);
        addToMap(variableMap, rawJson, "");
        VariableHolder<T> holder = constructor.construct(codec, variableMap, rawJson);
        return holder.validate(ops).map(h -> Pair.of(h, input));
    }

    private void addToMap(Map<String, Variable<?>> variableMap,
                          JsonElement input, String key) {
        Variable<?> variable = createPotentialVariable(input);
        if (variable != null) {
            variableMap.put(key, variable);
            return;
        }

        if (input.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entries : input.getAsJsonObject().asMap().entrySet()) {
                String newKey = entries.getKey();
                if (!key.isEmpty())
                    newKey = key + "." + newKey;
                addToMap(variableMap, entries.getValue(), newKey);
            }
        } else if (input.isJsonArray()) {
            JsonArray array = input.getAsJsonArray();
            for (int i = 0; i < array.size(); ++i) {
                String newKey = "[" + i + "]";
                if (!key.isEmpty())
                    newKey = key + "." + newKey;
                addToMap(variableMap, array.get(i), newKey);
            }
        }
    }

    @Nullable
    private Variable<?> createPotentialVariable(JsonElement value) {
        if (value.isJsonObject() && value.getAsJsonObject().has("effect_api:variable_type")) {
            return variableCodec.decode(JsonOps.INSTANCE, value).getOrThrow().getFirst();
        }
        return null;
    }

    @Override
    public <TOps> DataResult<TOps> encode(VariableHolder<T> input, DynamicOps<TOps> ops, TOps prefix) {
        ops.mergeToMap(prefix, ops.getMap(JsonOps.INSTANCE.convertTo(ops, input.getRawJson())).getOrThrow());
        return DataResult.success(prefix);
    }

    @FunctionalInterface
    public interface Constructor<T> {
        VariableHolder<T> construct(Codec<T> codec, Map<String, Variable<?>> variableMap, JsonElement rawJson);
    }
}