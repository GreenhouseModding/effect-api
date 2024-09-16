package house.greenhouse.effectapi.platform;

import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class EffectAPIPlatformHelperNeoForge implements EffectAPIPlatformHelper {

    @Override
    public EffectAPIPlatform getPlatform() {
        return EffectAPIPlatform.NEOFORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey) {
        return new RegistryBuilder<>(registryKey).create();
    }

    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public void sendClientbound(CustomPacketPayload payload, ServerPlayer player, boolean required) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendClientboundTracking(CustomPacketPayload payload, Entity entity, boolean required) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }
}