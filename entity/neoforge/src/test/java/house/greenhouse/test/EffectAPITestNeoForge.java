package house.greenhouse.test;

import house.greenhouse.test.attachment.PowersAttachment;
import house.greenhouse.test.command.TestCommand;
import house.greenhouse.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import house.greenhouse.test.platform.EffectAPITestHelperFabric;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(EffectAPITest.MOD_ID)
public class EffectAPITestNeoForge {
    public static final AttachmentType<PowersAttachment> POWERS = AttachmentType
            .builder(PowersAttachment::new)
            .serialize(PowersAttachment.CODEC)
            .copyOnDeath()
            .build();

    public EffectAPITestNeoForge(IEventBus eventBus) {
        EffectAPITest.init(new EffectAPITestHelperFabric());
    }

    @EventBusSubscriber(modid = EffectAPITest.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PowersAttachment.ID, () -> POWERS);
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0")
                    .playToClient(SyncPowerAttachmentClientboundPacket.TYPE, SyncPowerAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }

        @SubscribeEvent
        public static void createNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(EffectAPITest.POWER, Power.DIRECT_CODEC, Power.DIRECT_CODEC);
        }
    }

    @EventBusSubscriber(modid = EffectAPITest.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity().hasData(POWERS))
                event.getEntity().getData(POWERS).init(event.getEntity());
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
