package house.greenhouse.effectapi.entity.impl.network.clientbound;

import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
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
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeEntityResourceClientboundPacket<?>> STREAM_CODEC =
            StreamCodec.of(ChangeEntityResourceClientboundPacket::write, ChangeEntityResourceClientboundPacket::new);

    private final int entityId;
    private final Holder<Resource<T>> resource;
    private final Optional<ResourceLocation> source;
    private final Optional<T> value;

    public ChangeEntityResourceClientboundPacket(int entityId, Holder<Resource<T>> resource, Optional<ResourceLocation> source, Optional<T> value) {
        this.entityId = entityId;
        this.resource = resource;
        this.source = source;
        this.value = value;
    }

    public ChangeEntityResourceClientboundPacket(RegistryFriendlyByteBuf buf) {
        entityId = buf.readInt();
        resource = (Holder)ByteBufCodecs.holderRegistry(EffectAPIRegistryKeys.RESOURCE).decode(buf);
        source = ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).decode(buf);
        value = ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(resource.value().typeCodec())).decode(buf);
    }

    public static void write(RegistryFriendlyByteBuf buf, ChangeEntityResourceClientboundPacket<?> packet) {
        buf.writeInt(packet.entityId);
        ByteBufCodecs.holderRegistry(EffectAPIRegistryKeys.RESOURCE).encode(buf, (Holder)packet.resource);
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC).encode(buf, packet.source);
        ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(packet.resource.value().typeCodec())).encode(buf, (Optional)packet.value);
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (value.isEmpty()) {
                if (source.isEmpty()) {
                    EffectAPI.LOG.error("Attempted to remove resource \"" + resource.unwrapKey().get().location() + "\" without specifying a source.");
                    return;
                }
                EffectAPIEntity.getHelper().removeResource(entity, resource, source.get());
            } else
                EffectAPIEntity.getHelper().setResource(entity, resource, value.get(), source.orElse(null));
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
