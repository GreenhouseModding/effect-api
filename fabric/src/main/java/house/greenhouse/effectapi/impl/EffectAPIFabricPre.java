package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.platform.EffectAPIPlatformHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPIFabricPre implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        EffectAPI.init(new EffectAPIPlatformHelperFabric());
    }

}
