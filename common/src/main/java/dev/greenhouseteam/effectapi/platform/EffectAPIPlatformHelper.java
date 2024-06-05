package dev.greenhouseteam.effectapi.platform;

import dev.greenhouseteam.effectapi.api.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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

    <T> T setResource(Entity entity, ResourceLocation id, T value, @Nullable ResourceLocation source);

    void removeResource(Entity entity, ResourceLocation id, ResourceLocation source);

    @Nullable
    EntityEffectsAttachment getEntityEffects(Entity entity);

    void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source);

    void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source);

    void setEntityEffects(Entity entity, Map<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents);

    void sendClientboundTracking(CustomPacketPayload payload, Entity entity);
}