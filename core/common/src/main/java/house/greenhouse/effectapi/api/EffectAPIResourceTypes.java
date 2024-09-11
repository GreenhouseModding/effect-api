package house.greenhouse.effectapi.api;

import com.mojang.brigadier.arguments.*;
import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class EffectAPIResourceTypes {
    private static final Map<ResourceLocation, ArgumentType<?>> ARGUMENT_TYPES = new HashMap<>();

    public static void registerAll(RegistrationCallback<Codec<?>> callback) {
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("bool"), Codec.BOOL);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("double"), Codec.DOUBLE);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("float"), Codec.FLOAT);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("int"), Codec.INT);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("string"), Codec.STRING);

        registerArgumentTypes();
    }

    public static <T> ArgumentType<T> getArgumentType(Codec<T> resourceType) {
        ResourceLocation id = EffectAPIRegistries.RESOURCE_TYPE.getKey(resourceType);
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
