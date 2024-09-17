package house.greenhouse.test;

import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityRegistryKeys;
import house.greenhouse.test.attachment.PowersAttachment;
import house.greenhouse.test.command.TestCommand;
import house.greenhouse.test.effect.ParticleEffect;
import house.greenhouse.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import house.greenhouse.test.platform.EffectAPITestHelperNeoForge;
import house.greenhouse.test.variable.HealthVariable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(EffectAPIEntityTest.MOD_ID)
public class EffectAPITestNeoForge {
    public static final AttachmentType<PowersAttachment> POWERS = AttachmentType
            .builder(PowersAttachment::new)
            .serialize(PowersAttachment.CODEC)
            .copyOnDeath()
            .build();

    public EffectAPITestNeoForge(IEventBus eventBus) {
        EffectAPIEntityTest.init(new EffectAPITestHelperNeoForge());
    }

    @EventBusSubscriber(modid = EffectAPIEntityTest.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EffectAPIEntityTest.POWERS_ATTACHMENT_KEY, () -> POWERS);
            event.register(EffectAPIEntityRegistryKeys.EFFECT_COMPONENT_TYPE, EffectAPIEntityTest.asResource("particle"), () -> ParticleEffect.TYPE);
            event.register(EffectAPIRegistryKeys.VARIABLE_TYPE, EffectAPIEntityTest.asResource("health"), () -> HealthVariable.CODEC);
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0")
                    .playToClient(SyncPowerAttachmentClientboundPacket.TYPE, SyncPowerAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }

        @SubscribeEvent
        public static void createNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(EffectAPIEntityTest.POWER, Power.DIRECT_CODEC, Power.NETWORK_DIRECT_CODEC);
        }
    }

    @EventBusSubscriber(modid = EffectAPIEntityTest.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            ParticleEffect.tickParticles(event.getEntity());
        }

        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity().hasData(POWERS)) {
                PowersAttachment attachment = event.getEntity().getData(POWERS);
                attachment.init(event.getEntity());
                attachment.sync();
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity().hasData(POWERS)) {
                PowersAttachment attachment = event.getEntity().getData(POWERS);
                attachment.init(event.getEntity());
                attachment.sync();
            }
        }

        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getTarget().hasData(POWERS)) {
                event.getTarget().getData(POWERS).init(event.getTarget());
                event.getTarget().getData(POWERS).sync();
            }
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            TestCommand.register(event.getDispatcher(), event.getBuildContext());
        }
    }
}
