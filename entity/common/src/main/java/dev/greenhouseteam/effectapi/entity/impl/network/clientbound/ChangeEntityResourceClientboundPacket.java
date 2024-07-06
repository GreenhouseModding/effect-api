package dev.greenhouseteam.effectapi.entity.impl.network.clientbound;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.entity.impl.EffectAPIEntity;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class ChangeEntityResourceClientboundPacket<T> implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("change_entity_resource");
    public static final Type<ChangeEntityResourceClientboundPacket<?>> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeEntityResourceClientboundPacket<?>> STREAM_CODEC = StreamCodec.of(ChangeEntityResourceClientboundPacket::write, ChangeEntityResourceClientboundPacket::new);

    private final int entityId;
    private final ResourceEffect<T> resourceEffect;
    private final Optional<ResourceLocation> source;
    private final Optional<T> value;

    public ChangeEntityResourceClientboundPacket(int entityId, ResourceEffect<T> resourceEffect, Optional<ResourceLocation> source, Optional<T> value) {
        this.entityId = entityId;
        this.resourceEffect = resourceEffect;
        this.source = source;
        this.value = value;
    }

    public ChangeEntityResourceClientboundPacket(RegistryFriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.resourceEffect = InternalResourceUtil.getEffectFromId(buf.readResourceLocation());
        if (buf.readBoolean())
            this.source = Optional.of(buf.readResourceLocation());
        else
            this.source = Optional.empty();
        if (buf.readBoolean())
            this.value = Optional.of(resourceEffect.getResourceTypeCodec().fieldOf("value").codec().decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
        else
            this.value = Optional.empty();
    }

    public static void write(RegistryFriendlyByteBuf buf, ChangeEntityResourceClientboundPacket<?> packet) {
        buf.writeInt(packet.entityId);
        buf.writeResourceLocation(packet.resourceEffect.getId());
        buf.writeBoolean(packet.source.isPresent());
        packet.source.ifPresent(buf::writeResourceLocation);
        buf.writeBoolean(packet.value.isPresent());
        packet.value.ifPresent(object -> buf.writeNbt(((Codec<Object>) packet.resourceEffect.getResourceTypeCodec()).fieldOf("value").codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), object).getOrThrow()));
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (value.isEmpty() || source.isEmpty())
                EffectAPIEntity.getHelper().removeResource(entity, resourceEffect.getId(), source.orElse(null));
            else
                EffectAPIEntity.getHelper().setResource(entity, resourceEffect.getId(), value.get(), source.get());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
