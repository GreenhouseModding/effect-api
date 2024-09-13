package house.greenhouse.effectapi.entity.impl.network.clientbound;

import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SyncEntityResourcesAttachmentClientboundPacket(int entityId, ResourcesAttachmentImpl attachment) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("sync_entity_resources_attachment");
    public static final Type<SyncEntityResourcesAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityResourcesAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncEntityResourcesAttachmentClientboundPacket::write, SyncEntityResourcesAttachmentClientboundPacket::new);

    public SyncEntityResourcesAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ByteBufCodecs.fromCodecWithRegistries(ResourcesAttachmentImpl.CODEC).decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncEntityResourcesAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        ByteBufCodecs.fromCodecWithRegistries(ResourcesAttachmentImpl.CODEC).encode(buf, packet.attachment);
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
