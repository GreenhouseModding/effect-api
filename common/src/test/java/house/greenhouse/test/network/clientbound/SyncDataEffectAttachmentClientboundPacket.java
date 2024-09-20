package house.greenhouse.test.network.clientbound;

import house.greenhouse.test.EffectAPITest;
import house.greenhouse.test.DataEffect;
import house.greenhouse.test.attachment.DataEffectsAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record SyncDataEffectAttachmentClientboundPacket(int entityId, List<Holder<DataEffect>> allPowers) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPITest.asResource("sync_data_effect_attachment");
    public static final Type<SyncDataEffectAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDataEffectAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncDataEffectAttachmentClientboundPacket::write, SyncDataEffectAttachmentClientboundPacket::new);

    public SyncDataEffectAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ByteBufCodecs.holderRegistry(EffectAPITest.DATA_EFFECT).apply(ByteBufCodecs.list()).decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncDataEffectAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        ByteBufCodecs.holderRegistry(EffectAPITest.DATA_EFFECT).apply(ByteBufCodecs.list()).encode(buf, packet.allPowers);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity == null)
                return;
            DataEffectsAttachment attachment = EffectAPITest.getHelper().getDataEffects(entity);
            attachment.setFromNetwork(allPowers);
            if (allPowers.isEmpty())
                EffectAPITest.getHelper().removeDataEffectAttachment(entity);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
