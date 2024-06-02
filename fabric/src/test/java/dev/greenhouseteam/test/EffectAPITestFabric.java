package dev.greenhouseteam.test;

import dev.greenhouseteam.test.attachment.PowersAttachment;
import dev.greenhouseteam.test.command.TestCommand;
import dev.greenhouseteam.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import dev.greenhouseteam.test.platform.EffectAPITestHelperFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class EffectAPITestFabric implements ModInitializer {
    public static final AttachmentType<PowersAttachment> POWERS = AttachmentRegistry.<PowersAttachment>builder()
            .initializer(PowersAttachment::new)
            .persistent(PowersAttachment.CODEC)
            .copyOnDeath()
            .buildAndRegister(PowersAttachment.ID);

    @Override
    public void onInitialize() {
        EffectAPITest.init(new EffectAPITestHelperFabric());
        DynamicRegistries.registerSynced(EffectAPITest.POWER, Power.DIRECT_CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TestCommand.register(dispatcher, registryAccess));

        PayloadTypeRegistry.playS2C().register(SyncPowerAttachmentClientboundPacket.TYPE, SyncPowerAttachmentClientboundPacket.STREAM_CODEC);

        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(POWERS)) {
                trackedEntity.getAttached(POWERS).init(trackedEntity);
                trackedEntity.getAttached(POWERS).sync();
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (oldPlayer.hasAttached(POWERS))
                newPlayer.getAttached(POWERS).refresh();
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (entity.hasAttached(POWERS))
                entity.getAttached(POWERS).refresh();
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.hasAttached(POWERS))
                entity.getAttached(POWERS).init(entity);
        });
    }
}
