package dev.greenhouseteam.effectapi.impl.client;

import dev.greenhouseteam.effectapi.impl.entity.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.impl.entity.network.clientbound.SyncEffectsAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.impl.entity.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EffectAPIEntityFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChangeResourceClientboundPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEffectsAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncResourcesAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
    }
}
