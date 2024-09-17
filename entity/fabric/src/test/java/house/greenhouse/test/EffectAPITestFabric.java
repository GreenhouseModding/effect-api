package house.greenhouse.test;

import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityRegistries;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.test.attachment.PowersAttachment;
import house.greenhouse.test.command.TestCommand;
import house.greenhouse.test.effect.ParticleEffect;
import house.greenhouse.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
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
    public static final AttachmentType<PowersAttachment> POWERS = AttachmentRegistry.<PowersAttachment>builder()
            .initializer(PowersAttachment::new)
            .persistent(PowersAttachment.CODEC)
            .copyOnDeath()
            .buildAndRegister(EffectAPIEntityTest.POWERS_ATTACHMENT_KEY);

    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(EffectAPIEntityTest.POWER, Power.DIRECT_CODEC, Power.NETWORK_DIRECT_CODEC);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TestCommand.register(dispatcher, registryAccess));

        Registry.register(EffectAPIEntityRegistries.EFFECT_COMPONENT_TYPE, EffectAPIEntityTest.asResource("particle"), ParticleEffect.TYPE);
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, EffectAPIEntityTest.asResource("on_fire"), OnFirePredicate.TYPE);
        Registry.register(EffectAPIRegistries.VARIABLE_TYPE, EffectAPIEntityTest.asResource("health"), HealthVariable.CODEC);

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
