package house.greenhouse.test.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.EntityEffectAPI;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.phys.Vec3;

public record ParticleEffect(ParticleOptions particle, Vec3 speed, int tickRate) implements EffectAPIEffect {
    public static final Codec<ParticleEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(ParticleEffect::particle),
            Vec3.CODEC.optionalFieldOf("speed", Vec3.ZERO).forGetter(ParticleEffect::speed),
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("tick_rate", 1).forGetter(ParticleEffect::tickRate)
    ).apply(inst, ParticleEffect::new));
    public static final EffectType<ParticleEffect> TYPE = EffectType.<ParticleEffect>builder()
            .codec(CODEC)
            .build();

    public static void tickParticles(Entity entity) {
        if (!entity.level().isClientSide)
            return;
        EntityEffectAPI.getEffects(entity, ParticleEffect.TYPE).forEach(effect -> {
            if (entity.tickCount % effect.tickRate == 0)
                entity.level().addParticle(effect.particle, entity.getX(), entity.getY(0.5), entity.getZ(), effect.speed.x, effect.speed.y, effect.speed.z);
        });
    }

    @Override
    public boolean shouldTick(LootContext context, boolean isActive, int tickCount) {
        return isActive && tickCount % tickRate == 0;
    }

    @Override
    public EffectType<?, Entity> type() {
        return TYPE;
    }
}
