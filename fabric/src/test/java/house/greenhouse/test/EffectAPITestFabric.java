package house.greenhouse.test;

import house.greenhouse.effectapi.impl.registry.EffectAPIRegistries;
import house.greenhouse.test.attachment.DataEffectsAttachment;
import house.greenhouse.test.command.TestCommand;
import house.greenhouse.test.effect.ParticleEffect;
import house.greenhouse.test.network.clientbound.SyncDataEffectAttachmentClientboundPacket;
import house.greenhouse.test.predicate.OnFirePredicate;
import house.greenhouse.test.variable.HealthVariable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EffectAPITestFabric implements ModInitializer {
    public static final AttachmentType<DataEffectsAttachment> DATA_EFFECTS = AttachmentRegistry.<DataEffectsAttachment>builder()
            .initializer(DataEffectsAttachment::new)
            .persistent(DataEffectsAttachment.CODEC)
            .copyOnDeath()
            .buildAndRegister(EffectAPITest.DATA_EFFECTS_ATTACHMENT_KEY);

    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(EffectAPITest.DATA_EFFECT, DataEffect.DIRECT_CODEC, DataEffect.NETWORK_DIRECT_CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> TestCommand.register(dispatcher, buildContext));

        Registry.register(EffectAPIRegistries.EFFECT_TYPE, EffectAPITest.asResource("particle"), ParticleEffect.TYPE);
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, EffectAPITest.asResource("on_fire"), OnFirePredicate.TYPE);
        Registry.register(EffectAPIRegistries.VARIABLE_TYPE, EffectAPITest.asResource("health"), HealthVariable.CODEC);

        PayloadTypeRegistry.playS2C().register(SyncDataEffectAttachmentClientboundPacket.TYPE, SyncDataEffectAttachmentClientboundPacket.STREAM_CODEC);

        EntityTrackingEvents.START_TRACKING.register((trackedEntity, player) -> {
            if (trackedEntity.hasAttached(DATA_EFFECTS)) {
                trackedEntity.getAttached(DATA_EFFECTS).init(trackedEntity);
                trackedEntity.getAttached(DATA_EFFECTS).sync();
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.hasAttached(DATA_EFFECTS)) {
                entity.getAttached(DATA_EFFECTS).init(entity);
                entity.getAttached(DATA_EFFECTS).sync();
            }
        });
    }
}
