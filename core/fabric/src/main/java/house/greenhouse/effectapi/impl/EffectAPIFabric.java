package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.api.EffectAPIResourceTypes;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;

public class EffectAPIFabric implements ModInitializer {
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        DynamicRegistries.registerSynced(EffectAPIRegistryKeys.RESOURCE, Resource.DIRECT_CODEC);
        EffectAPIResourceTypes.registerAll(Registry::register);
        EffectAPIAttachments.init();

        ServerLifecycleEvents.SERVER_STARTED.register(EffectAPIFabric::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server1 -> {
            setServer(null);
        });
    }

    public static void setServer(MinecraftServer server) {
        EffectAPIFabric.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
