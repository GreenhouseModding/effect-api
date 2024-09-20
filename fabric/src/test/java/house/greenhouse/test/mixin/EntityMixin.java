package house.greenhouse.test.mixin;

import house.greenhouse.test.effect.ParticleEffect;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void effect_api_entity_test$tickParticleEffects(CallbackInfo ci) {
        ParticleEffect.tickParticles((Entity)(Object)this);
    }
}
