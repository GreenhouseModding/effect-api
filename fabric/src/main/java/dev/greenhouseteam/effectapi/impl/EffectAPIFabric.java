package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.attachment.EffectsAttachment;
import dev.greenhouseteam.effectapi.api.command.DataResourceArgument;
import dev.greenhouseteam.effectapi.api.command.DataResourceValueArgument;
import dev.greenhouseteam.effectapi.api.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncEffectsClientboundPacket;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;

public class EffectAPIFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        registerContents();

        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncResourcesAttachmentClientboundPacket(trackedEntity.getId(), trackedEntity.getAttached(EffectAPIAttachments.RESOURCES)), trackedEntity);

            if (trackedEntity.hasAttached(EffectAPIAttachments.EFFECTS)) {
                trackedEntity.getAttached(EffectAPIAttachments.EFFECTS).init(trackedEntity);
                trackedEntity.getAttached(EffectAPIAttachments.EFFECTS).sync();
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.hasAttached(EffectAPIAttachments.EFFECTS)) {
                EffectsAttachment attachment = newPlayer.getAttached(EffectAPIAttachments.EFFECTS);
                attachment.refresh();
                attachment.onRespawn();
                if (attachment.isEmpty())
                    newPlayer.removeAttached(EffectAPIAttachments.EFFECTS);
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity.hasAttached(EffectAPIAttachments.EFFECTS))
                entity.getAttached(EffectAPIAttachments.EFFECTS).refresh();
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.hasAttached(EffectAPIAttachments.EFFECTS))
                entity.getAttached(EffectAPIAttachments.EFFECTS).init(entity);
        });

    }

    public static void registerContents() {
        EffectAPIAttachments.init();
        EffectAPIEffectTypes.registerAll(Registry::register);
        EffectAPIInstancedEffectTypes.registerAll(Registry::register);
        EffectAPIResourceTypes.registerAll(Registry::register);

        PayloadTypeRegistry.playS2C().register(ChangeResourceClientboundPacket.TYPE, ChangeResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEffectsClientboundPacket.TYPE, SyncEffectsClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncResourcesAttachmentClientboundPacket.TYPE, SyncResourcesAttachmentClientboundPacket.STREAM_CODEC);

        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource_value"), DataResourceValueArgument.class, new DataResourceValueArgument.Info());
        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource"), DataResourceArgument.class, SingletonArgumentInfo.contextFree(DataResourceArgument::resource));
    }
}
