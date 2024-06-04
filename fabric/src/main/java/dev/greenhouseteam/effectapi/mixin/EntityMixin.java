package dev.greenhouseteam.effectapi.mixin;

import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract Level level();

    @Inject(method = "tick", at = @At("TAIL"))
    private void effectapitest$tick(CallbackInfo ci) {
        if (!level().isClientSide() && ((Entity)(Object)this).hasAttached(EffectAPIAttachments.EFFECTS))
            ((Entity)(Object)this).getAttached(EffectAPIAttachments.EFFECTS).tick();
    }
}
