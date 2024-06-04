package dev.greenhouseteam.effectapi.mixin;

import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We run before Fabric's event to make sure that modders can use it.
@Mixin(targets = "net/minecraft/server/level/ServerLevel$EntityCallbacks", priority = 500)
public class ServerLevelEntityCallbacksMixin {
    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
        if (entity.hasAttached(EffectAPIAttachments.EFFECTS)) {
            entity.getAttached(EffectAPIAttachments.EFFECTS).init(entity);
            entity.getAttached(EffectAPIAttachments.EFFECTS).sync();
        }
    }
}
