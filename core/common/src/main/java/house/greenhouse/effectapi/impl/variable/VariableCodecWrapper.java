package house.greenhouse.effectapi.impl.variable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import house.greenhouse.effectapi.api.variable.JsonReference;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        Map<List<JsonReference>, Variable<?>> variableMap = new HashMap<>();
        JsonElement rawJson = ops.convertTo(JsonOps.INSTANCE, input);
        addToMap(variableMap, rawJson, new ArrayList<>());
        VariableHolder<T> holder = constructor.construct(codec, variableMap, rawJson);
        return holder.validate(ops).map(h -> Pair.of(h, input));
    }

    private void addToMap(Map<List<JsonReference>, Variable<?>> variableMap,
                          JsonElement input, List<JsonReference> keys) {
        Variable<?> variable = createPotentialVariable(input);
        if (variable != null) {
            variableMap.put(keys, variable);
            return;
        }

        if (input.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entries : input.getAsJsonObject().asMap().entrySet()) {
                List<JsonReference> newKeys = new ArrayList<>(keys);
                newKeys.add(JsonReference.createObject(entries.getKey()));
                addToMap(variableMap, entries.getValue(), newKeys);
            }
        } else if (input.isJsonArray()) {
            JsonArray array = input.getAsJsonArray();
            for (int i = 0; i < array.size(); ++i) {
                List<JsonReference> newKeys = new ArrayList<>(keys);
                newKeys.add(JsonReference.createArrayValue(i));
                addToMap(variableMap, array.get(i), newKeys);
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
        VariableHolder<T> construct(Codec<T> codec, Map<List<JsonReference>, Variable<?>> variableMap, JsonElement rawJson);
    }
}