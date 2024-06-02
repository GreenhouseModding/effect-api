package dev.greenhouseteam.effectapi.api;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sun.jdi.connect.Connector;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
