package dev.greenhouseteam.effectapi.entity.impl.network.clientbound;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.entity.impl.EffectAPIEntity;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SyncEntityResourcesAttachmentClientboundPacket(int entityId, ResourcesAttachment attachment) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("sync_entity_resources_attachment");
    public static final Type<SyncEntityResourcesAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityResourcesAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncEntityResourcesAttachmentClientboundPacket::write, SyncEntityResourcesAttachmentClientboundPacket::new);

    public SyncEntityResourcesAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ResourcesAttachment.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncEntityResourcesAttachmentClientboundPacket packet) {
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
