package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.platform.EffectAPIEntityPlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPIEntity.init(new EffectAPIEntityPlatformHelperFabric());
    }
}
