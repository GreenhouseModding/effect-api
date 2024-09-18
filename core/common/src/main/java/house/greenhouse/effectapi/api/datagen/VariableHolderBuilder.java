package house.greenhouse.effectapi.api.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import house.greenhouse.effectapi.api.variable.JsonReference;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.variable.VariableHolderImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableHolderBuilder<T> {
    private final Codec<T> baseCodec;
    private final Codec<Variable<?>> variableCodec;
    private T base;
    private final Map<List<JsonReference>, Variable<?>> variableMap = new HashMap<>();

    protected VariableHolderBuilder(Codec<T> baseCodec, Codec<Variable<?>> variableCodec) {
        this.baseCodec = baseCodec;
        this.variableCodec = variableCodec;
    }

    public static <T> VariableHolderBuilder<T> builder(Codec<T> baseCodec, Codec<Variable<?>> variableCodec) {
        return new VariableHolderBuilder<>(baseCodec, variableCodec);
    }

    public VariableHolderBuilder<T> base(T base) {
        this.base = base;
        return this;
    }

    public <V> VariableHolderBuilder<T> withVariable(List<JsonReference> keys, Variable<V> variable) {
        variableMap.put(keys, variable);
        return this;
    }

    public VariableHolder<T> build() {
        if (base == null)
            throw new IllegalStateException("Cannot create VariableHolder without a base value");
        return new VariableHolderImpl<>(baseCodec, variableMap, createRawJson());
    }

    private JsonElement createRawJson() {
        JsonElement output = baseCodec.encodeStart(JsonOps.INSTANCE, base).getOrThrow();
        for (Map.Entry<List<JsonReference>, Variable<?>> entry : variableMap.entrySet())
            encodeVariable(output, entry.getKey(), entry.getValue());
        return output;
    }

    private void encodeVariable(JsonElement output, List<JsonReference> keys, Variable<?> variable) {
        JsonElement currentElement = output;
        for (int i = 0; i < keys.size(); ++i) {
            JsonReference key = keys.get(i);
            if (i == keys.size() - 1) {
                JsonElement newElement = variableCodec.encodeStart(JsonOps.INSTANCE, variable).getOrThrow();
                if (key.isCombined()) {
                    currentElement.getAsJsonObject().get(key.key()).getAsJsonArray().set(key.index(), newElement);
                } else if (key.isArray()) {
                    currentElement.getAsJsonArray().set(key.index(), newElement);
                } else
                    currentElement.getAsJsonObject().add(key.key(), newElement);
                break;
            }
            if (key.isCombined()) {
                currentElement = currentElement.getAsJsonObject().get(key.key()).getAsJsonArray().get(key.index());
            } else if (key.isArray()) {
                currentElement = currentElement.getAsJsonArray().get(key.index());
            } else
                currentElement = currentElement.getAsJsonObject().get(key.key());
        }
    }
}
