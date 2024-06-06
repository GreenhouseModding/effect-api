package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.platform.EffectAPIBasePlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIBaseFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPI.init(new EffectAPIBasePlatformHelperFabric());
    }
}
