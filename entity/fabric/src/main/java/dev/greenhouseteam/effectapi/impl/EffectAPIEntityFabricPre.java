package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import dev.greenhouseteam.effectapi.platform.EffectAPIEntityPlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIEntityFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPIEntity.init(new EffectAPIEntityPlatformHelperFabric());
    }
}
