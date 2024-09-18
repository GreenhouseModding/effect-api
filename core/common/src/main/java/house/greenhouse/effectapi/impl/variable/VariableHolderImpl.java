package house.greenhouse.effectapi.impl.variable;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import house.greenhouse.effectapi.api.variable.JsonReference;
import house.greenhouse.effectapi.api.variable.Variable;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableHolderImpl<T> implements VariableHolder<T> {
    private final Codec<T> codec;
    private final Map<List<JsonReference>, Variable<?>> variableMap;
    private final JsonElement rawJson;

    public VariableHolderImpl(Codec<T> codec, Map<List<JsonReference>, Variable<?>> variableMap, JsonElement rawJson) {
        this.codec = codec;
        this.variableMap = variableMap;
        this.rawJson = rawJson;
    }

    public Codec<T> getInnerCodec() {
        return codec;
    }

    public Map<List<JsonReference>, Object> getPreviousValues(LootContext context) {
        return variableMap.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().get(context))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public T construct(LootContext context, Map<List<JsonReference>, Object> variableValues) {
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
        Map<List<JsonReference>, Object> values = variableMap.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue().dataType().validationValue())).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));;
        var encoded = encodeValues(values);
        var dataResult = getInnerCodec().decode(ops, JsonOps.INSTANCE.convertTo(ops, encoded));
        return dataResult.map(pair -> this);
    }

    @Override
    public Map<List<JsonReference>, Variable<?>> getVariables() {
        return variableMap;
    }

    @Override
    public JsonElement getRawJson() {
        return rawJson;
    }

    private JsonElement encodeValues(Map<List<JsonReference>, Object> values) {
        JsonElement output = rawJson.deepCopy();
        for (Map.Entry<List<JsonReference>, Variable<?>> entry : variableMap.entrySet())
            encodeVariable(output, entry.getKey(), entry.getValue(), values.get(entry.getKey()));
        return output;
    }

    private void encodeVariable(JsonElement output, List<JsonReference> keys, Variable<?> variable, Object value) {
        JsonElement currentElement = output;
        for (int i = 0; i < keys.size(); ++i) {
            JsonReference key = keys.get(i);
            if (i == keys.size() - 1) {
                JsonElement newElement = ((Codec<Object>)variable.dataType().codec()).encodeStart(JsonOps.INSTANCE, value).getOrThrow();
                if (key.index() > -1) {
                    currentElement.getAsJsonArray().set(key.index(), newElement);
                } else
                    currentElement.getAsJsonObject().add(key.key(), newElement);
                break;
            }
            if (key.index() > -1) {
                currentElement = currentElement.getAsJsonArray().get(key.index());
            } else
                currentElement = currentElement.getAsJsonObject().get(key.key());
        }
    }
}