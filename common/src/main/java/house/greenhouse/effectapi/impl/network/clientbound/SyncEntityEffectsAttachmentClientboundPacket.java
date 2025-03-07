package house.greenhouse.effectapi.impl.network.clientbound;

import house.greenhouse.effectapi.api.EffectAPICodecs;
import house.greenhouse.effectapi.impl.registry.EffectAPIEffectTypes;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record SyncEntityEffectsAttachmentClientboundPacket(int entityId, DataComponentMap combinedComponents, DataComponentMap activeComponents) implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("sync_entity_effects_attachment");
    public static final Type<SyncEntityEffectsAttachmentClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityEffectsAttachmentClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncEntityEffectsAttachmentClientboundPacket::write, SyncEntityEffectsAttachmentClientboundPacket::new);

    public SyncEntityEffectsAttachmentClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), ByteBufCodecs.fromCodecWithRegistries(EffectAPICodecs.EFFECT_MAP).decode(buf), ByteBufCodecs.fromCodecWithRegistries(EffectAPICodecs.EFFECT_MAP).decode(buf));
    }

    public static void write(RegistryFriendlyByteBuf buf, SyncEntityEffectsAttachmentClientboundPacket packet) {
        buf.writeInt(packet.entityId);
        ByteBufCodecs.fromCodecWithRegistries(EffectAPICodecs.EFFECT_MAP).encode(buf, packet.combinedComponents);
        ByteBufCodecs.fromCodecWithRegistries(EffectAPICodecs.EFFECT_MAP).encode(buf, packet.activeComponents);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity == null)
                return;
            EffectAPI.getHelper().setEntityEffects(entity, combinedComponents, activeComponents);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
