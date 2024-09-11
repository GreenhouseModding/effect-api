package house.greenhouse.effectapi.entity.impl;

import house.greenhouse.effectapi.entity.platform.EffectAPIEntityPlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIEntityFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPIEntity.init(new EffectAPIEntityPlatformHelperFabric());
    }
}
