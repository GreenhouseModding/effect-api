package house.greenhouse.effectapi.impl.variable;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Map;
import java.util.stream.Collectors;

public class VariableHolderImpl<T> implements VariableHolder<T> {
    private final Codec<T> codec;
    private final Map<String, Variable<?>> variableMap;
    private final JsonElement rawJson;

    public VariableHolderImpl(Codec<T> codec, Map<String, Variable<?>> variableMap, JsonElement rawJson) {
        this.codec = codec;
        this.variableMap = variableMap;
        this.rawJson = rawJson;
    }

    public Codec<T> getInnerCodec() {
        return codec;
    }

    public Map<String, Object> getPreviousValues(LootContext context) {
        return variableMap.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().get(context))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public T construct(LootContext context, Map<String, Object> variableValues) {
        var dataResult = getInnerCodec().decode(RegistryOps.create(JsonOps.INSTANCE, context.getLevel().registryAccess()), encodeValues(variableValues));
        if (dataResult.isError()) {
            EffectAPI.LOG.error("Failed to construct object from variables: {}", dataResult.error().get().message());
            return null;
        }
        T val = dataResult.getOrThrow().getFirst();;
        return val;
    }

    @Override
    public <TOps> DataResult<VariableHolder<T>> validate(DynamicOps<TOps> ops) {
        Map<String, Object> values = variableMap.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().dataType().validationValue())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));;
        var encoded = encodeValues(values);
        var dataResult = getInnerCodec().decode(ops, JsonOps.INSTANCE.convertTo(ops, encoded));
        return dataResult.map(pair -> this);
    }

    @Override
    public Map<String, Variable<?>> getVariables() {
        return variableMap;
    }

    @Override
    public JsonElement getRawJson() {
        return rawJson;
    }

    private JsonElement encodeValues(Map<String, Object> values) {
        JsonElement output = rawJson.deepCopy();
        for (Map.Entry<String, Variable<?>> entry : variableMap.entrySet()) {
            encodeVariable(output, entry.getKey(), entry.getValue(), values.get(entry.getKey()));
        }
        return output;
    }

    private void encodeVariable(JsonElement original, String key, Variable<?> variable, Object value) {
        JsonElement currentElement = original;
        String[] split = key.split("\\.");
        for (int i = 0; i < split.length; ++i) {
            String string = split[i];
            if (i == split.length - 1) {
                JsonElement newElement = ((Codec<Object>)variable.dataType().codec()).encodeStart(JsonOps.INSTANCE, value).getOrThrow();
                if (string.matches("\\[([0-9]+)]")) {
                    int listIndex = Integer.parseInt(string.substring(1).substring(0, string.length() - 2));
                    currentElement.getAsJsonArray().set(listIndex, newElement);
                } else
                    currentElement.getAsJsonObject().add(string, newElement);
                break;
            }
            if (string.matches("\\[([0-9]+)]")) {
                int listIndex = Integer.parseInt(string.substring(1).substring(0, string.length() - 2));
                currentElement = currentElement.getAsJsonArray().get(listIndex);
            } else
                currentElement = currentElement.getAsJsonObject().get(string);
        }
    }
}