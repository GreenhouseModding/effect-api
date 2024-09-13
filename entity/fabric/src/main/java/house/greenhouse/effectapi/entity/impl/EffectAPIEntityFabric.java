package house.greenhouse.effectapi.entity.impl;

import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityActionTypes;
import house.greenhouse.effectapi.entity.api.command.EntityResourceArgument;
import house.greenhouse.effectapi.entity.api.command.EntityResourceValueArgument;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityPredicates;
import house.greenhouse.effectapi.entity.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import house.greenhouse.effectapi.entity.impl.registry.EffectAPIEntityAttachments;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class EffectAPIEntityFabric implements ModInitializer {
    private static final ResourceLocation EFFECT_API_BEFORE_EVENT = EffectAPI.asResource("before");

    @Override
    public void onInitialize() {
        registerContents();
    }

    public static void registerContents() {
        EffectAPIEntityAttachments.init();
        EffectAPIEntityEffectTypes.registerAll(Registry::register);
        EffectAPIEntityActionTypes.registerAll(Registry::register);
        EffectAPIEntityPredicates.registerAll(Registry::register);

        PayloadTypeRegistry.playS2C().register(ChangeEntityResourceClientboundPacket.TYPE, ChangeEntityResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityEffectsAttachmentClientboundPacket.TYPE, SyncEntityEffectsAttachmentClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncEntityResourcesAttachmentClientboundPacket.TYPE, SyncEntityResourcesAttachmentClientboundPacket.STREAM_CODEC);

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(EFFECT_API_BEFORE_EVENT, Event.DEFAULT_PHASE);
        ServerEntityEvents.ENTITY_LOAD.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS)) {
                trackedEntity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).init(trackedEntity);
                trackedEntity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).sync();
            }
            if (trackedEntity.hasAttached(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(trackedEntity.getId(), trackedEntity.getAttached(EffectAPIAttachments.RESOURCES)), trackedEntity);
        });
        EntityTrackingEvents.START_TRACKING.addPhaseOrdering(EFFECT_API_BEFORE_EVENT, Event.DEFAULT_PHASE);
        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS)) {
                trackedEntity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).init(trackedEntity);
                trackedEntity.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).sync();
            }
            if (trackedEntity.hasAttached(EffectAPIAttachments.RESOURCES))
                EffectAPI.getHelper().sendClientboundTracking(new SyncEntityResourcesAttachmentClientboundPacket(trackedEntity.getId(), trackedEntity.getAttached(EffectAPIAttachments.RESOURCES)), trackedEntity);
        });
        ServerPlayerEvents.COPY_FROM.addPhaseOrdering(EFFECT_API_BEFORE_EVENT, Event.DEFAULT_PHASE);
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (newPlayer.hasAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS))
                newPlayer.getAttached(EffectAPIEntityAttachments.ENTITY_EFFECTS).refresh();
        });

        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource"), EntityResourceArgument.class, new EntityResourceArgument.Info());
        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource_value"), EntityResourceValueArgument.class, new EntityResourceValueArgument.Info());
    }
}
