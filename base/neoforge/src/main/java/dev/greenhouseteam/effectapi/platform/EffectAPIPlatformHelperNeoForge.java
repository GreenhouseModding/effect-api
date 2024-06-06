package dev.greenhouseteam.effectapi.platform;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegistryBuilder;

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
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).orElse(null);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setData(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, ResourceLocation id, T value) {
        return entity.getData(EffectAPIAttachments.RESOURCES).setValue(id, value);
    }

    @Override
    public void removeResource(Entity entity, ResourceLocation id) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.resources().remove(id);
        if (attachment.resources().isEmpty())
            entity.removeData(EffectAPIAttachments.RESOURCES);
    }

    @Override
    public void sendClientboundTracking(CustomPacketPayload payload, Entity entity) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }
}