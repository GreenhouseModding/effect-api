package house.greenhouse.effectapi.api.variable;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.effectapi.impl.variable.VariableHolderImplClient;
import house.greenhouse.effectapi.impl.variable.VariableCodecWrapper;
import house.greenhouse.effectapi.impl.variable.VariableHolderImpl;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;
import java.util.Map;

/**
 * Represents a variable holder.
 * This is used on the server to create new objects based on variables.
 * On the client, this is a placeholder, but is implemented anyway to allow for encoding/decoding values from the server/to the client.
 * If you are implementing something client-friendly, you should sync the constructed objects instead of the variable holders.
 *
 * @param <T>   The constructed object's type.
 */
public interface VariableHolder<T> {
    /**
     * Wraps a map codec to allow its value to become a VariableHolder.
     * @param codec The codec to wrap.
     * @return      A variable holder.
     * @param <T>   The type that the variable holder will construct.
     */
    static <T> Codec<VariableHolder<T>> wrapCodec(Codec<T> codec, Codec<Variable<?>> variableCodec) {
        return new VariableCodecWrapper<>(codec, variableCodec, VariableHolderImpl::new);
    }

    /**
     * A client friendly wrapped map codec for synced data pack/dynamic registries.
     *
     *
     * @param codec The codec to wrap.
     * @return      A variable holder that will return a placeholder variable holder mainly used for syncing and deserialization.
     * @param <T>   The type that the variable holder will construct.
     */
    static <T> Codec<VariableHolder<T>> wrapCodecForNetwork(Codec<T> codec, Codec<Variable<?>> variableCodec) {
        return new VariableCodecWrapper<>(codec, variableCodec, VariableHolderImplClient::new);
    }

    Map<List<JsonReference>, Object> getPreviousValues(LootContext context);

    /**
     * Computes the variable, creating a new object.
     * This is unnecessary to call on the client,
     *
     * @param context           The loot context for this variable.
     * @return                  Either a computed value or the fallback value if they would be the exact same.
     */
    T construct(LootContext context, Map<List<JsonReference>, Object> variableValues);


    <TOps> DataResult<VariableHolder<T>> validate(DynamicOps<TOps> ops);

    /**
     * Every variable for the constructor's reference.
     * The key is an array of strings, with each value after the first index signifying an inner object.
     */
    Map<List<JsonReference>, Variable<?>> getVariables();

    /**
     * The raw json of this VariableHolder, including both variable fields and non*variable fields.
     */
    JsonElement getRawJson();
}
