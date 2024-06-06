package dev.greenhouseteam.effectapi.api.entity.network.clientbound;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SyncResourcesAttachmentClientboundPacket(int entityId, ResourcesAttachment attachment) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPIEntity.asResource("sync_resources_attachment");
    public static final Type<SyncResourcesAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncResourcesAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncResourcesAttachmentClientboundPacket::write, SyncResourcesAttachmentClientboundPacket::new);

    public SyncResourcesAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ResourcesAttachment.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncResourcesAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeNbt(ResourcesAttachment.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.attachment).getOrThrow());
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            EffectAPIEntity.getHelper().setResourcesAttachment(entity, attachment);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
