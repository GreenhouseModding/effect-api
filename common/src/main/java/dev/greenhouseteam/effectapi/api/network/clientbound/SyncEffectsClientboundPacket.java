package dev.greenhouseteam.effectapi.api.network.clientbound;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.attachment.EffectSource;
import dev.greenhouseteam.effectapi.api.attachment.EffectsAttachment;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Map;

public record SyncEffectsClientboundPacket(int entityId, Map<ResourceLocation, DataComponentMap> allEffects, List<EffectSource> sources, DataComponentMap activeEffects) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("sync_effects");
    public static final Type<SyncEffectsClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEffectsClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncEffectsClientboundPacket::write, SyncEffectsClientboundPacket::new);

    public SyncEffectsClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), Codec.unboundedMap(ResourceLocation.CODEC, EffectAPIEffectTypes.CODEC).decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst(), EffectSource.CODEC.listOf().fieldOf("sources").codec().decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst(), EffectAPIEffectTypes.CODEC.decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncEffectsClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        buf.writeNbt(Codec.unboundedMap(ResourceLocation.CODEC, EffectAPIEffectTypes.CODEC).encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.allEffects).getOrThrow());
        buf.writeNbt(EffectSource.CODEC.listOf().fieldOf("sources").codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.sources).getOrThrow());
        buf.writeNbt(EffectAPIEffectTypes.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.activeEffects).getOrThrow());
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            EffectAPI.getHelper().setEffects(entity, EffectsAttachment.loadMap(allEffects, sources), activeEffects);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
