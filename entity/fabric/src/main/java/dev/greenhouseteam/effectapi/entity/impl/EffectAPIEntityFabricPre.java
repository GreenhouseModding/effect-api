package dev.greenhouseteam.effectapi.entity.impl;

import dev.greenhouseteam.effectapi.entity.impl.entity.EffectAPIEntity;
import dev.greenhouseteam.effectapi.entity.platform.EffectAPIEntityPlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIEntityFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPIEntity.init(new EffectAPIEntityPlatformHelperFabric());
    }
}
