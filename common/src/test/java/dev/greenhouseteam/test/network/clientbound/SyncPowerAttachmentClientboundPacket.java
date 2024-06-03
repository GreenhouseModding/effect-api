package dev.greenhouseteam.test.network.clientbound;

import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.test.EffectAPITest;
import dev.greenhouseteam.test.Power;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public record SyncPowerAttachmentClientboundPacket(int entityId, List<Holder<Power>> allPowers, DataComponentMap activeComponents) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPITest.asResource("sync_power_attachment");
    public static final Type<SyncPowerAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPowerAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncPowerAttachmentClientboundPacket::write, SyncPowerAttachmentClientboundPacket::new);

    public SyncPowerAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), Power.CODEC.listOf().fieldOf("powers").codec().decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst(), EffectAPIEffectTypes.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncPowerAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeNbt(Power.CODEC.listOf().fieldOf("powers").codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.allPowers).getOrThrow());
        buf.writeNbt(EffectAPIEffectTypes.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.activeComponents).getOrThrow());
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            EffectAPITest.getHelper().getPowers(entity).setFromNetwork(allPowers, activeComponents);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
