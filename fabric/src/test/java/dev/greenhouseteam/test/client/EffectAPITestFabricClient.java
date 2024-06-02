package dev.greenhouseteam.test.client;

import dev.greenhouseteam.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EffectAPITestFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncPowerAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
    }
}
