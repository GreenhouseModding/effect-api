package house.greenhouse.effectapi.platform;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.resource.Resource;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
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

    MinecraftServer getServer();

    default void sendClientbound(CustomPacketPayload payload, ServerPlayer player) {
        sendClientbound(payload, player, false);
    }
    void sendClientbound(CustomPacketPayload payload, ServerPlayer player, boolean required);

    default void sendClientboundTracking(CustomPacketPayload payload, Entity entity) {
        sendClientboundTracking(payload, entity, false);
    }

    void sendClientboundTracking(CustomPacketPayload payload, Entity entity, boolean required);

    @Nullable
    ResourcesAttachment getResources(Entity entity);

    <T> boolean hasResource(Entity entity, Holder<Resource<T>> resource);

    void setResourcesAttachment(Entity entity, ResourcesAttachment attachment);

    @Nullable
    <T> T setResource(Entity entity, Holder<Resource<T>> resource, T value, @Nullable ResourceLocation source);

    <T> void removeResource(Entity entity, Holder<Resource<T>> resource, ResourceLocation source);

    @Nullable
    EffectsAttachment getEntityEffects(Entity entity);

    void addEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source);

    void removeEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source);

    void setEntityEffects(Entity entity, DataComponentMap combinedComponents, DataComponentMap activeComponents);
}