package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.platform.EffectAPIPlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPI.init(new EffectAPIPlatformHelperFabric());
    }
}
