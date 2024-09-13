package house.greenhouse.effectapi.impl.client;

import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

public class EffectAPINeoForgeClient {
    @EventBusSubscriber(modid = EffectAPI.MOD_ID, bus = EventBusSubscriber.Bus.GAME,  value = Dist.CLIENT)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
            if (event.getPlayer() != null) {
                InternalResourceUtil.clearEffectMap();
            }
        }
    }
}
