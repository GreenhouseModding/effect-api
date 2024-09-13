package house.greenhouse.effectapi.entity.impl.network.clientbound;

import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
        entityId = buf.readInt();
        resourceEffect = InternalResourceUtil.getEffectFromId(buf.readResourceLocation());
        source = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
        value = ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(resourceEffect.getResourceTypeCodec())).decode(buf);
    }

    public static void write(RegistryFriendlyByteBuf buf, ChangeEntityResourceClientboundPacket<?> packet) {
        buf.writeInt(packet.entityId);
        buf.writeResourceLocation(packet.resourceEffect.getId());
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, packet.source);
        ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(packet.resourceEffect.getResourceTypeCodec())).encode(buf, (Optional)packet.value);
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
