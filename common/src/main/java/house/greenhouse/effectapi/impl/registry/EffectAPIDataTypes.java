package house.greenhouse.effectapi.impl.registry;

import com.mojang.brigadier.arguments.*;
import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

import java.util.HashMap;
import java.util.Map;

public class EffectAPIDataTypes {
    private static final Map<DataType<?>, ArgumentType<?>> ARGUMENT_TYPES = new HashMap<>();

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
    }

    public static void registerArgumentTypes() {
        registerArgumentType(BOOL, BoolArgumentType.bool());
        registerArgumentType(DOUBLE, DoubleArgumentType.doubleArg());
        registerArgumentType(FLOAT, FloatArgumentType.floatArg());
        registerArgumentType(INT, IntegerArgumentType.integer());
        registerArgumentType(STRING, StringArgumentType.string());
    }

    public static <T> void registerArgumentType(DataType<T> dataType, ArgumentType<T> argumentType) {
        if (ARGUMENT_TYPES.containsKey(dataType))
            throw new UnsupportedOperationException("An argument type for \"" + EffectAPIRegistries.DATA_TYPE.getKey(dataType) + "\" has already been registered.");
        ARGUMENT_TYPES.put(dataType, argumentType);
    }

    public static <T> ArgumentType<T> getArgumentType(DataType<T> dataType) {
        if (!ARGUMENT_TYPES.containsKey(dataType))
            throw new NullPointerException("Could not find argument type for resource type \"" + EffectAPIRegistries.DATA_TYPE.getKey(dataType) + "\". Please check that an associated argument type has been registered.");
        return (ArgumentType<T>) ARGUMENT_TYPES.get(dataType);
    }
}
