package house.greenhouse.effectapi.platform;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

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


    @Override
    public ResourcesAttachment getResources(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).orElse(null);
    }

    @Override
    public <T> boolean hasResource(Entity entity, Holder<Resource<T>> resource) {
        return entity.getExistingData(EffectAPIAttachments.RESOURCES).map(attachment -> attachment.hasResource(resource)).orElse(false);
    }

    @Override
    public void setResourcesAttachment(Entity entity, ResourcesAttachment attachment) {
        entity.setData(EffectAPIAttachments.RESOURCES, attachment);
    }

    @Override
    public <T> T setResource(Entity entity, Holder<Resource<T>> resource, T value, @Nullable ResourceLocation source) {
        if (source == null && !entity.hasData(EffectAPIAttachments.RESOURCES))
            return null;
        ResourcesAttachment attachment = entity.getData(EffectAPIAttachments.RESOURCES);
        return attachment.setValue(resource, value, source);
    }

    @Override
    public <T> void removeResource(Entity entity, Holder<Resource<T>> resource, ResourceLocation source) {
        ResourcesAttachment attachment = getResources(entity);
        if (attachment == null)
            return;
        attachment.removeValue(resource, source);
        if (attachment.isEmpty())
            entity.removeData(EffectAPIAttachments.RESOURCES);
    }

    @Override
    public @Nullable EffectsAttachment getEntityEffects(Entity entity) {
        return entity.getExistingData(EffectAPIAttachments.EFFECTS).orElse(null);
    }

    @Override
    public void addEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        if (entity.hasData(EffectAPIAttachments.EFFECTS) && entity.getData(EffectAPIAttachments.EFFECTS).hasEffect(effect, true))
            return;
        EffectsAttachmentImpl attachment = (EffectsAttachmentImpl) entity.getData(EffectAPIAttachments.EFFECTS);
        attachment.init(entity);
        attachment.addEffect(effect, source);
    }

    @Override
    public void removeEntityEffect(Entity entity, EffectHolder<EffectAPIEffect> effect, ResourceLocation source) {
        var attachment = entity.getExistingData(EffectAPIAttachments.EFFECTS);
        if (attachment.isEmpty() || !attachment.get().hasEffect(effect, true))
            return;
        attachment.get().removeEffect(effect, source);
        if (attachment.get().isEmpty())
            entity.removeData(EffectAPIAttachments.EFFECTS);
    }

    @Override
    public void setEntityEffects(Entity entity, DataComponentMap combinedComponents, DataComponentMap activeComponents) {
        if (combinedComponents.isEmpty())
            entity.removeData(EffectAPIAttachments.EFFECTS);
        else
            ((EffectsAttachmentImpl)entity.getData(EffectAPIAttachments.EFFECTS)).setComponents(combinedComponents, activeComponents);
    }
}