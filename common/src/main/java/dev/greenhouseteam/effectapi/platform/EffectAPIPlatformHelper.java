package dev.greenhouseteam.effectapi.platform;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface EffectAPIPlatformHelper {

    EffectAPIPlatform getPlatform();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    <T> Registry<T> createRegistry(ResourceKey<Registry<T>> registryKey);

    @Nullable
    ResourcesAttachment getResources(Entity entity);

    void setResourcesAttachment(Entity entity, ResourcesAttachment attachment);

    <T> T setResource(Entity entity, ResourceLocation id, T value);

    void removeResource(Entity entity, ResourceLocation id);

    void sendClientboundTracking(CustomPacketPayload payload, Entity entity);
}