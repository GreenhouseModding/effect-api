package house.greenhouse.effectapi.api;

import com.mojang.brigadier.arguments.*;
import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class EffectAPIDataTypes {
    private static final Map<ResourceLocation, ArgumentType<?>> ARGUMENT_TYPES = new HashMap<>();

    public static final DataType<Boolean> BOOL = new DataType<>(Codec.BOOL, false);
    public static final DataType<Double> DOUBLE = new DataType<>(Codec.DOUBLE, 0.0D);
    public static final DataType<Float> FLOAT = new DataType<>(Codec.FLOAT, 0.0F);
    public static final DataType<Integer> INT = new DataType<>(Codec.INT, 0);
    public static final DataType<String> STRING = new DataType<>(Codec.STRING, "");

    public static void registerAll(RegistrationCallback<DataType<?>> callback) {
        callback.register(EffectAPIRegistries.DATA_TYPE, EffectAPI.asResource("bool"), BOOL);
        callback.register(EffectAPIRegistries.DATA_TYPE, EffectAPI.asResource("double"), DOUBLE);
        callback.register(EffectAPIRegistries.DATA_TYPE, EffectAPI.asResource("float"), FLOAT);
        callback.register(EffectAPIRegistries.DATA_TYPE, EffectAPI.asResource("int"), INT);
        callback.register(EffectAPIRegistries.DATA_TYPE, EffectAPI.asResource("string"), STRING);

        registerArgumentTypes();
    }

    public static <T> ArgumentType<T> getArgumentType(DataType<T> dataType) {
        ResourceLocation id = EffectAPIRegistries.DATA_TYPE.getKey(dataType);
        if (!ARGUMENT_TYPES.containsKey(id))
            throw new NullPointerException("Could not find argument type for resource type '" + id + "'. Please check that an associated argument type has been registered.");
        return (ArgumentType<T>) ARGUMENT_TYPES.get(id);
    }

    public static void registerArgumentTypes() {
        ARGUMENT_TYPES.put(EffectAPI.asResource("bool"), BoolArgumentType.bool());
        ARGUMENT_TYPES.put(EffectAPI.asResource("double"), DoubleArgumentType.doubleArg());
        ARGUMENT_TYPES.put(EffectAPI.asResource("float"), FloatArgumentType.floatArg());
        ARGUMENT_TYPES.put(EffectAPI.asResource("int"), IntegerArgumentType.integer());
        ARGUMENT_TYPES.put(EffectAPI.asResource("string"), StringArgumentType.string());
    }
}
