package dev.greenhouseteam.effectapi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

public class EffectAPIResourceTypes {

    public static void registerAll(RegistrationCallback<Codec<?>> callback) {
        callback.register(EffectAPIRegistries.RESOURCE_TYPE, EffectAPI.asResource("int"), Codec.INT);
    }
}
