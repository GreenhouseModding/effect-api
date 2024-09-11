package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.platform.EffectAPIBasePlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIBaseFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPI.init(new EffectAPIBasePlatformHelperFabric());
    }

}
