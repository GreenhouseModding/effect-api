package dev.greenhouseteam.effectapi.api.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record EffectSource(ResourceLocation id, boolean persistsOnRespawn) {
    public static final Codec<EffectSource> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(EffectSource::id),
            Codec.BOOL.optionalFieldOf("persists_on_respawn", false).forGetter(EffectSource::persistsOnRespawn)
    ).apply(inst, EffectSource::new));

    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;
        if (!(object instanceof EffectSource other))
            return false;
        return other.persistsOnRespawn == persistsOnRespawn && other.id.equals(id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, persistsOnRespawn);
    }
}
