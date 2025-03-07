package house.greenhouse.effectapi.impl.registry;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.api.modifier.AddModifier;
import house.greenhouse.effectapi.api.modifier.ClampModifier;
import house.greenhouse.effectapi.api.modifier.DivideModifier;
import house.greenhouse.effectapi.api.modifier.MaxModifier;
import house.greenhouse.effectapi.api.modifier.MinModifier;
import house.greenhouse.effectapi.api.modifier.MultiplyModifier;
import house.greenhouse.effectapi.api.modifier.Modifier;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;

public class EffectAPIModifierTypes {

    public static void registerAll(RegistrationCallback<MapCodec<? extends Modifier>> callback) {
        callback.register(EffectAPIRegistries.MODIFIER, EffectAPI.asResource("add"), AddModifier.CODEC);
        callback.register(EffectAPIRegistries.MODIFIER, EffectAPI.asResource("clamp"), ClampModifier.CODEC);
        callback.register(EffectAPIRegistries.MODIFIER, EffectAPI.asResource("divide"), DivideModifier.CODEC);
        callback.register(EffectAPIRegistries.MODIFIER, EffectAPI.asResource("max"), MaxModifier.CODEC);
        callback.register(EffectAPIRegistries.MODIFIER, EffectAPI.asResource("min"), MinModifier.CODEC);
        callback.register(EffectAPIRegistries.MODIFIER, EffectAPI.asResource("multiply"), MultiplyModifier.CODEC);
    }
}
