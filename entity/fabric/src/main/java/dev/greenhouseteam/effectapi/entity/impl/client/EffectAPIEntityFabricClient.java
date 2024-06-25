package dev.greenhouseteam.effectapi.entity.impl.client;

import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.entity.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EffectAPIEntityFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChangeEntityResourceClientboundPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEntityEffectsAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEntityResourcesAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
    }
}
