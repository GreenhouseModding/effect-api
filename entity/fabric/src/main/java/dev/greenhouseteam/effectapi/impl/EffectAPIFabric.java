package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.entity.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.api.entity.EffectAPIEntityInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.entity.command.EntityResourceArgument;
import dev.greenhouseteam.effectapi.api.entity.command.EntityResourceValueArgument;
import dev.greenhouseteam.effectapi.api.entity.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.entity.network.clientbound.SyncEffectsAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.api.entity.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIEntityAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;

public class EffectAPIFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        registerContents();
    }

    public static void registerContents() {
        EffectAPIEntityAttachments.init();
        EffectAPIEntityEffectTypes.registerAll(Registry::register);
        EffectAPIEntityInstancedEffectTypes.registerAll(Registry::register);
        EffectAPIResourceTypes.registerAll(Registry::register);

        PayloadTypeRegistry.playS2C().register(ChangeResourceClientboundPacket.TYPE, ChangeResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEffectsAttachmentClientboundPacket.TYPE, SyncEffectsAttachmentClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncResourcesAttachmentClientboundPacket.TYPE, SyncResourcesAttachmentClientboundPacket.STREAM_CODEC);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.hasAttached(EffectAPIEntityAttachments.EFFECTS))
                newPlayer.getAttached(EffectAPIEntityAttachments.EFFECTS).refresh();
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity.hasAttached(EffectAPIEntityAttachments.EFFECTS))
                entity.getAttached(EffectAPIEntityAttachments.EFFECTS).refresh();
        });

        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource"), EntityResourceArgument.class, new EntityResourceArgument.Info());
        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource_value"), EntityResourceValueArgument.class, new EntityResourceValueArgument.Info());
    }
}
