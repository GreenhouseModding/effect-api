package dev.greenhouseteam.effectapi.entity.impl;

import dev.greenhouseteam.effectapi.entity.api.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.entity.api.EffectAPIEntityInstancedEffectTypes;
import dev.greenhouseteam.effectapi.entity.api.command.EntityResourceArgument;
import dev.greenhouseteam.effectapi.entity.api.command.EntityResourceValueArgument;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.registry.EffectAPIEntityAttachments;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;

public class EffectAPIEntityFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        registerContents();
    }

    public static void registerContents() {
        EffectAPIEntityAttachments.init();
        EffectAPIEntityEffectTypes.registerAll(Registry::register);
        EffectAPIEntityInstancedEffectTypes.registerAll(Registry::register);

        PayloadTypeRegistry.playS2C().register(ChangeEntityResourceClientboundPacket.TYPE, ChangeEntityResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityEffectsAttachmentClientboundPacket.TYPE, SyncEntityEffectsAttachmentClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityResourcesAttachmentClientboundPacket.TYPE, SyncEntityResourcesAttachmentClientboundPacket.STREAM_CODEC);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS))
                newPlayer.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).refresh();
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS))
                entity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).refresh();
        });

        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource"), EntityResourceArgument.class, new EntityResourceArgument.Info());
        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource_value"), EntityResourceValueArgument.class, new EntityResourceValueArgument.Info());
    }
}
