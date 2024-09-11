package house.greenhouse.test;

import house.greenhouse.test.attachment.PowersAttachment;
import house.greenhouse.test.command.TestCommand;
import house.greenhouse.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
        DynamicRegistries.registerSynced(EffectAPITest.POWER, Power.DIRECT_CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TestCommand.register(dispatcher, registryAccess));

        PayloadTypeRegistry.playS2C().register(SyncPowerAttachmentClientboundPacket.TYPE, SyncPowerAttachmentClientboundPacket.STREAM_CODEC);

        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(POWERS)) {
                trackedEntity.getAttached(POWERS).init(trackedEntity);
                trackedEntity.getAttached(POWERS).sync();
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.hasAttached(POWERS)) {
                entity.getAttached(POWERS).init(entity);
                entity.getAttached(POWERS).sync();
            }
        });
    }
}
