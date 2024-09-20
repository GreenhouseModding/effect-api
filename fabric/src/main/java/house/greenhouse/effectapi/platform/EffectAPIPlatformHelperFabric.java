package house.greenhouse.effectapi.platform;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.EffectAPIFabric;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
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

import java.util.Optional;

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

    @Override
    @Nullable
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getAttached(EffectAPIAttachments.RESOURCES);
    }

    @Override
    public <T> boolean hasResource(Entity entity, Holder<Resource<T>> resource) {
        return Optional.ofNullable(entity.getAttached(EffectAPIAttachments.RESOURCES)).map(attachment -> attachment.hasResource(resource)).orElse(false);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setAttached(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, Holder<Resource<T>> resource, T value, ResourceLocation source) {
        if (source == null && !entity.hasAttached(EffectAPIAttachments.RESOURCES))
            return null;
        ResourcesAttachment attachment = entity.getAttachedOrCreate(EffectAPIAttachments.RESOURCES);
        return attachment.setValue(resource, value, source);
    }

    @Override
    public <T> void removeResource(Entity entity, Holder<Resource<T>> resource, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.removeValue(resource, source);
        if (attachment.isEmpty())
            entity.removeAttached(EffectAPIAttachments.RESOURCES);
    }


    @Override
    public @Nullable EffectsAttachment getEntityEffects(Entity entity) {
        return entity.getAttached(EffectAPIAttachments.EFFECTS);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (entity.hasAttached(EffectAPIAttachments.EFFECTS) && entity.getAttached(EffectAPIAttachments.EFFECTS).hasEffect(effect, true))
            return;
        EffectsAttachment attachment =  entity.getAttachedOrCreate(EffectAPIAttachments.EFFECTS);
        ((EffectsAttachmentImpl)attachment).init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        EffectsAttachment attachment =  entity.getAttached(EffectAPIAttachments.EFFECTS);
        if (attachment == null || !attachment.hasEffect(effect, true))
            return;
        attachment.removeEffect(effect, source);
        if (attachment.isEmpty())
            entity.removeAttached(EffectAPIAttachments.EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, DataComponentMap combinedComponents, DataComponentMap activeComponents) {
        if (combinedComponents.isEmpty())
            entity.removeAttached(EffectAPIAttachments.EFFECTS);
        else
            ((EffectsAttachmentImpl)entity.getAttachedOrCreate(EffectAPIAttachments.EFFECTS)).setComponents(combinedComponents, activeComponents);
    }
}
