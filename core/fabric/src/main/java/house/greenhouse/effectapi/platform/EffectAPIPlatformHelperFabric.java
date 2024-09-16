package house.greenhouse.effectapi.platform;

import house.greenhouse.effectapi.impl.EffectAPIFabric;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EffectAPIPlatformHelperFabric implements EffectAPIPlatformHelper {
    @Override
    public EffectAPIPlatform getPlatform() {
        return EffectAPIPlatform.FABRIC;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }

    @Override
    public MinecraftServer getServer() {
        return EffectAPIFabric.getServer();
    }

    @Override
    public void sendClientbound(CustomPacketPayload payload, ServerPlayer player, boolean required) {
        if (required || ServerPlayNetworking.canSend(player, payload.type()))
            ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendClientboundTracking(CustomPacketPayload payload, Entity entity, boolean required) {
        for (ServerPlayer other : PlayerLookup.tracking(entity)) {
            if (required || ServerPlayNetworking.canSend(other, payload.type()))
                ServerPlayNetworking.send(other, payload);
        }
        if (entity instanceof ServerPlayer player)
            if (required || ServerPlayNetworking.canSend(player, payload.type()))
                ServerPlayNetworking.send(player, payload);
    }
}
