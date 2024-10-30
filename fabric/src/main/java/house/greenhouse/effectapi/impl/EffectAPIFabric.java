package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import house.greenhouse.effectapi.impl.registry.EffectAPIDataTypes;
import house.greenhouse.effectapi.impl.registry.EffectAPIEffectTypes;
import house.greenhouse.effectapi.impl.registry.EffectAPIModifierTypes;
import house.greenhouse.effectapi.impl.registry.EffectAPIVariableTypes;
import house.greenhouse.effectapi.api.command.EntityResourceArgument;
import house.greenhouse.effectapi.api.command.EntityResourceValueArgument;
import house.greenhouse.effectapi.impl.registry.EffectAPIPredicates;
import house.greenhouse.effectapi.impl.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public class EffectAPIFabric implements ModInitializer {
    private static MinecraftServer server;
    private static final ResourceLocation EFFECT_API_BEFORE_EVENT = EffectAPI.asResource("before");

    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(EffectAPIRegistryKeys.RESOURCE, Resource.DIRECT_CODEC);
        EffectAPIActionTypes.registerAll(Registry::register);
        EffectAPIDataTypes.registerAll(Registry::register);
        EffectAPIDataTypes.registerArgumentTypes();
        EffectAPIEffectTypes.registerAll(Registry::register);
        EffectAPIModifierTypes.registerAll(Registry::register);
        EffectAPIPredicates.registerAll(Registry::register);
        EffectAPIVariableTypes.registerAll(Registry::register);
        EffectAPIAttachments.init();

        ServerLifecycleEvents.SERVER_STARTED.register(EffectAPIFabric::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server1 -> {
            setServer(null);
        });

        registerEffectAttachmentEvents();

        PayloadTypeRegistry.playS2C().register(ChangeEntityResourceClientboundPacket.TYPE, ChangeEntityResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityEffectsAttachmentClientboundPacket.TYPE, SyncEntityEffectsAttachmentClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityResourcesAttachmentClientboundPacket.TYPE, SyncEntityResourcesAttachmentClientboundPacket.STREAM_CODEC);

        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("resource"), EntityResourceArgument.class, new EntityResourceArgument.Info());
        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("resource_value"), EntityResourceValueArgument.class, new EntityResourceValueArgument.Info());
    }

    private static void registerEffectAttachmentEvents() {
        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(EFFECT_API_BEFORE_EVENT, Event.DEFAULT_PHASE);
        ServerEntityEvents.ENTITY_LOAD.register(EFFECT_API_BEFORE_EVENT, (trackedEntity, player) -> {
            if (trackedEntity.hasAttached(EffectAPIAttachments.EFFECTS)) {
                EffectsAttachmentImpl attachment = (EffectsAttachmentImpl) trackedEntity.getAttached(EffectAPIAttachments.EFFECTS);
                attachment.init(trackedEntity);
                attachment.refresh();
                attachment.sync();
            }
            if (trackedEntity.hasAttached(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(trackedEntity.getId(), trackedEntity.getAttached(EffectAPIAttachments.RESOURCES)), trackedEntity);
        });
        EntityTrackingEvents.START_TRACKING.addPhaseOrdering(EFFECT_API_BEFORE_EVENT, Event.DEFAULT_PHASE);
        EntityTrackingEvents.START_TRACKING.register(EFFECT_API_BEFORE_EVENT, (trackedEntity, player) -> {
            if (trackedEntity.hasAttached(EffectAPIAttachments.EFFECTS)) {
                EffectsAttachmentImpl attachment = (EffectsAttachmentImpl) trackedEntity.getAttached(EffectAPIAttachments.EFFECTS);
                attachment.sync(player);
            }
            if (trackedEntity.hasAttached(EffectAPIAttachments.RESOURCES))
                ServerPlayNetworking.send(player, new SyncEntityResourcesAttachmentClientboundPacket(trackedEntity.getId(), trackedEntity.getAttached(EffectAPIAttachments.RESOURCES)));
        });
        ServerPlayerEvents.AFTER_RESPAWN.addPhaseOrdering(EFFECT_API_BEFORE_EVENT, Event.DEFAULT_PHASE);
        ServerPlayerEvents.AFTER_RESPAWN.register(EFFECT_API_BEFORE_EVENT, (oldPlayer, newPlayer, alive) -> {
            if (oldPlayer.hasAttached(EffectAPIAttachments.EFFECTS)) {
                newPlayer.setAttached(EffectAPIAttachments.EFFECTS, oldPlayer.getAttached(EffectAPIAttachments.EFFECTS));
                EffectsAttachmentImpl attachment = (EffectsAttachmentImpl) newPlayer.getAttached(EffectAPIAttachments.EFFECTS);
                attachment.init(newPlayer);
                attachment.refresh();
                attachment.sync();
            }
        });
    }

    public static void setServer(MinecraftServer server) {
        EffectAPIFabric.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
