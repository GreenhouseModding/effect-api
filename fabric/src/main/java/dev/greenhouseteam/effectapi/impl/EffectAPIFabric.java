package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.EffectAPIInstancedEffectTypes;
import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.command.DataResourceArgument;
import dev.greenhouseteam.effectapi.api.command.DataResourceValueArgument;
import dev.greenhouseteam.effectapi.api.network.clientbound.ChangeResourceClientboundPacket;
import dev.greenhouseteam.effectapi.api.network.clientbound.SyncResourcesAttachmentClientboundPacket;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;

public class EffectAPIFabric implements ModInitializer {
    
    @Override
    public void onInitialize() {
        registerContents();
    }

    public static void registerContents() {
        EffectAPIAttachments.init();
        EffectAPIEffectTypes.registerAll(Registry::register);
        EffectAPIInstancedEffectTypes.registerAll(Registry::register);
        EffectAPIResourceTypes.registerAll(Registry::register);

        PayloadTypeRegistry.playS2C().register(ChangeResourceClientboundPacket.TYPE, ChangeResourceClientboundPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(SyncResourcesAttachmentClientboundPacket.TYPE, SyncResourcesAttachmentClientboundPacket.STREAM_CODEC);

        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource"), DataResourceArgument.class, SingletonArgumentInfo.contextFree(DataResourceArgument::resource));
        ArgumentTypeRegistry.registerArgumentType(EffectAPI.asResource("data_resource_value"), DataResourceValueArgument.class, new DataResourceValueArgument.Info());
    }
}
