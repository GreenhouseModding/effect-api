package house.greenhouse.effectapi.entity.impl.network.clientbound;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EffectAPIEffectTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;

public record SyncEntityEffectsAttachmentClientboundPacket(int entityId, Map<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("sync_entity_effects_attachment");
    public static final Type<SyncEntityEffectsAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityEffectsAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncEntityEffectsAttachmentClientboundPacket::write, SyncEntityEffectsAttachmentClientboundPacket::new);

    public SyncEntityEffectsAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), Codec.unboundedMap(ResourceLocation.CODEC, EffectAPIEntityEffectTypes.codec(EffectAPIEntityLootContextParamSets.ENTITY)).decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst(), EffectAPIEntityEffectTypes.codec(EffectAPIEntityLootContextParamSets.ENTITY).decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncEntityEffectsAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess());
        buf.writeNbt(Codec.unboundedMap(ResourceLocation.CODEC, EffectAPIEntityEffectTypes.codec(EffectAPIEntityLootContextParamSets.ENTITY)).encodeStart(ops, packet.alLComponents).getOrThrow());
        buf.writeNbt(EffectAPIEntityEffectTypes.codec(EffectAPIEntityLootContextParamSets.ENTITY).encodeStart(ops, packet.activeComponents).getOrThrow());
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            EffectAPIEntity.getHelper().setEntityEffects(entity, alLComponents, activeComponents);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
