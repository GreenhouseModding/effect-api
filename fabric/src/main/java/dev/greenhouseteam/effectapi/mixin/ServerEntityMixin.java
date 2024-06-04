package dev.greenhouseteam.effectapi.mixin;

import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We run before Fabric's event to make sure that modders can use it.
@Mixin(value = ServerEntity.class, priority = 500)
public class ServerEntityMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "addPairing", at = @At("HEAD"))
    private void onStartTracking(ServerPlayer player, CallbackInfo ci) {
        if (entity.hasAttached(EffectAPIAttachments.EFFECTS)) {
            entity.getAttached(EffectAPIAttachments.EFFECTS).init(entity);
            entity.getAttached(EffectAPIAttachments.EFFECTS).sync();
        }
    }
}
