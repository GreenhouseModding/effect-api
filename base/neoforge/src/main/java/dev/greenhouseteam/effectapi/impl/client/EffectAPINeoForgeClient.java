package dev.greenhouseteam.effectapi.impl.client;

import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class EffectAPINeoForgeClient {
    @EventBusSubscriber(modid = EffectAPI.MOD_ID, bus = EventBusSubscriber.Bus.GAME,  value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            ResourceEffect.clearEffectMap();
        }
    }
}
