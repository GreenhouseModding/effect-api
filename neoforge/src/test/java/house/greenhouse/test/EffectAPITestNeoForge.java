package house.greenhouse.test;

import house.greenhouse.effectapi.api.registry.EffectAPIRegistries;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.test.attachment.DataEffectsAttachment;
import house.greenhouse.test.command.TestCommand;
import house.greenhouse.test.effect.ParticleEffect;
import house.greenhouse.test.network.clientbound.SyncDataEffectAttachmentClientboundPacket;
import house.greenhouse.test.platform.EffectAPITestHelperNeoForge;
import house.greenhouse.test.predicate.OnFirePredicate;
import house.greenhouse.test.variable.HealthVariable;
import net.minecraft.core.registries.Registries;
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

@Mod(EffectAPITest.MOD_ID)
public class EffectAPITestNeoForge {
    public static final AttachmentType<DataEffectsAttachment> DATA_EFFECTS = AttachmentType
            .builder(DataEffectsAttachment::new)
            .serialize(DataEffectsAttachment.CODEC)
            .copyOnDeath()
            .build();

    public EffectAPITestNeoForge(IEventBus eventBus) {
        EffectAPITest.init(new EffectAPITestHelperNeoForge());
    }

    @EventBusSubscriber(modid = EffectAPITest.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerContent(RegisterEvent event) {
            event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EffectAPITest.DATA_EFFECTS_ATTACHMENT_KEY, () -> DATA_EFFECTS);
            event.register(EffectAPIRegistryKeys.EFFECT_TYPE, EffectAPITest.asResource("particle"), () -> ParticleEffect.TYPE);
            event.register(Registries.LOOT_CONDITION_TYPE, EffectAPITest.asResource("on_fire"), () -> OnFirePredicate.TYPE);
            event.register(EffectAPIRegistryKeys.VARIABLE_TYPE, EffectAPITest.asResource("health"), () -> HealthVariable.CODEC);
        }

        @SubscribeEvent
        public static void registerPackets(RegisterPayloadHandlersEvent event) {
            event.registrar("1.0.0")
                    .playToClient(SyncDataEffectAttachmentClientboundPacket.TYPE, SyncDataEffectAttachmentClientboundPacket.STREAM_CODEC, (payload, context) -> payload.handle());
        }

        @SubscribeEvent
        public static void createNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
            event.dataPackRegistry(EffectAPITest.DATA_EFFECT, DataEffect.DIRECT_CODEC, DataEffect.NETWORK_DIRECT_CODEC);
        }
    }

    @EventBusSubscriber(modid = EffectAPITest.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            ParticleEffect.tickParticles(event.getEntity());
        }

        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity().hasData(DATA_EFFECTS)) {
                DataEffectsAttachment attachment = event.getEntity().getData(DATA_EFFECTS);
                attachment.init(event.getEntity());
                attachment.sync();
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity().hasData(DATA_EFFECTS)) {
                DataEffectsAttachment attachment = event.getEntity().getData(DATA_EFFECTS);
                attachment.init(event.getEntity());
                attachment.sync();
            }
        }

        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (event.getTarget().hasData(DATA_EFFECTS)) {
                event.getTarget().getData(DATA_EFFECTS).init(event.getTarget());
                event.getTarget().getData(DATA_EFFECTS).sync();
            }
        }

        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            TestCommand.register(event.getDispatcher(), event.getBuildContext());
        }
    }
}
