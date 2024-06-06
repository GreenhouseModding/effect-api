package dev.greenhouseteam.effectapi.impl.client;

import dev.greenhouseteam.effectapi.impl.util.InternalResourceUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;

public class EffectAPIBaseFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> InternalResourceUtil.clearEffectMap());
    }
}
