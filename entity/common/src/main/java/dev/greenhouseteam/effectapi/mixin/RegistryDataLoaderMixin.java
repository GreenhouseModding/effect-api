package dev.greenhouseteam.effectapi.mixin;

import dev.greenhouseteam.effectapi.api.effect.EntityResourceEffect;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
    @Inject(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;", at = @At("HEAD"))
    private static void effectapi$setRegistryPhase(RegistryDataLoader.LoadingFunction loadingFunction, RegistryAccess access, List<RegistryDataLoader.RegistryData<?>> data, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        EntityResourceEffect.EffectCodec.setRegistryPhase(true);
    }

    @Inject(method = "load(Lnet/minecraft/resources/RegistryDataLoader$LoadingFunction;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;", at = @At("RETURN"))
    private static void effectapi$resetRegistryPhase(RegistryDataLoader.LoadingFunction loadingFunction, RegistryAccess access, List<RegistryDataLoader.RegistryData<?>> data, CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
        EntityResourceEffect.EffectCodec.setRegistryPhase(false);
        EntityResourceEffect.EffectCodec.clearLoadedIds();
    }
}
