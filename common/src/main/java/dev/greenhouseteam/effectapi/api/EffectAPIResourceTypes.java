package dev.greenhouseteam.effectapi.api;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIResourceTypes {

    public static void registerAll(RegistrationCallback<Codec<?>> callback) {
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("bool"), Codec.BOOL);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("double"), Codec.DOUBLE);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("float"), Codec.FLOAT);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("int"), Codec.INT);
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("string"), Codec.STRING);
    }
}
