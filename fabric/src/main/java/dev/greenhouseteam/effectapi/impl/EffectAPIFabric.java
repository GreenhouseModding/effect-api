package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIEffects;
import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.api.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.core.Registry;

public class EffectAPIFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        registerContents();

        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncResourcesAttachmentClientboundPacket(trackedEntity.getId(), trackedEntity.getAttached(EffectAPIAttachments.RESOURCES)), trackedEntity);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ResourceEffect.EffectCodec.setRegistryPhase(false);
            ResourceEffect.EffectCodec.clearLoadedIds();
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ResourceEffect.EffectCodec.setRegistryPhase(true));
    }

    public static void registerContents() {
        EffectAPIAttachments.init();
        EffectAPIEffects.registerAll(Registry::register);
        EffectAPIResourceTypes.registerAll(Registry::register);

        PayloadTypeRegistry.playS2C().register(ChangeResourceClientboundPacket.TYPE, ChangeResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncResourcesAttachmentClientboundPacket.TYPE, SyncResourcesAttachmentClientboundPacket.STREAM_CODEC);
    }
}
