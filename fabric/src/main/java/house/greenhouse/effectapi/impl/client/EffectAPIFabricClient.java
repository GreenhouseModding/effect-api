package house.greenhouse.effectapi.impl.client;

import house.greenhouse.effectapi.impl.network.clientbound.ChangeEntityResourceClientboundPacket;
import house.greenhouse.effectapi.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.network.clientbound.SyncEntityResourcesAttachmentClientboundPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class EffectAPIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChangeEntityResourceClientboundPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEntityEffectsAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncEntityResourcesAttachmentClientboundPacket.TYPE, (packet, context) -> packet.handle());
    }
}