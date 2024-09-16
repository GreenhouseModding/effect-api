package house.greenhouse.test.network.clientbound;

import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.test.EffectAPIEntityTest;
import house.greenhouse.test.Power;
import house.greenhouse.test.attachment.PowersAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record SyncPowerAttachmentClientboundPacket(int entityId, List<Holder<Power>> allPowers) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPIEntityTest.asResource("sync_power_attachment");
    public static final Type<SyncPowerAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPowerAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncPowerAttachmentClientboundPacket::write, SyncPowerAttachmentClientboundPacket::new);

    public SyncPowerAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ByteBufCodecs.holderRegistry(EffectAPIEntityTest.POWER).apply(ByteBufCodecs.list()).decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncPowerAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        ByteBufCodecs.holderRegistry(EffectAPIEntityTest.POWER).apply(ByteBufCodecs.list()).encode(buf, packet.allPowers);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            PowersAttachment attachment = EffectAPIEntityTest.getHelper().getPowers(entity);
            attachment.setFromNetwork(allPowers);
            if (allPowers.isEmpty())
                EffectAPIEntityTest.getHelper().removePowerAttachment(entity);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
