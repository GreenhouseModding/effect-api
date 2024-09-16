package house.greenhouse.effectapi.platform;

import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface EffectAPIPlatformHelper {

    EffectAPIPlatform getPlatform();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey);

    MinecraftServer getServer();

    default void sendClientbound(CustomPacketPayload payload, ServerPlayer player) {
        sendClientbound(payload, player, false);
    }
    void sendClientbound(CustomPacketPayload payload, ServerPlayer player, boolean required);

    default void sendClientboundTracking(CustomPacketPayload payload, Entity entity) {
        sendClientboundTracking(payload, entity, false);
    }

    void sendClientboundTracking(CustomPacketPayload payload, Entity entity, boolean required);
}