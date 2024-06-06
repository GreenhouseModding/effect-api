package dev.greenhouseteam.effectapi.platform;

import dev.greenhouseteam.effectapi.api.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIEntityAttachments;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class EffectAPIEntityPlatformHelperFabric implements EffectAPIPlatformHelper {
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
    @Nullable
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getAttached(EffectAPIEntityAttachments.RESOURCES);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setAttached(EffectAPIEntityAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, ResourceLocation id, T value, ResourceLocation source) {
        return entity.getAttachedOrCreate(EffectAPIEntityAttachments.RESOURCES).setValue(id, value, source);
    }

    @Override
    public void removeResource(Entity entity, ResourceLocation id, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.removeValue(id);
        if (attachment.resources().isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.RESOURCES);
    }


    @Override
    public @Nullable EntityEffectsAttachment getEntityEffects(Entity entity) {
        return entity.getAttached(EffectAPIEntityAttachments.EFFECTS);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        EntityEffectsAttachment attachment =  entity.getAttachedOrCreate(EffectAPIEntityAttachments.EFFECTS);
        attachment.init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectAPIEffect effect, ResourceLocation source) {
        EntityEffectsAttachment attachment =  entity.getAttached(EffectAPIEntityAttachments.EFFECTS);
        if (attachment == null)
            return;
        attachment.removeEffect(effect, source);
        if (attachment.isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, Map<ResourceLocation, DataComponentMap> alLComponents, DataComponentMap activeComponents) {
        if (alLComponents.isEmpty())
            entity.removeAttached(EffectAPIEntityAttachments.EFFECTS);
        else
            entity.getAttachedOrCreate(EffectAPIEntityAttachments.EFFECTS).setComponents(alLComponents, activeComponents);
    }

    @Override
    public void sendClientboundTracking(CustomPacketPayload payload, Entity entity) {
        for (ServerPlayer other : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(other, payload);
        }
        if (entity instanceof ServerPlayer player)
            ServerPlayNetworking.send(player, payload);
    }
}
