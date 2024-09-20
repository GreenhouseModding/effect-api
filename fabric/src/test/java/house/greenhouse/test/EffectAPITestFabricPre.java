package house.greenhouse.test;

import house.greenhouse.test.platform.EffectAPITestHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPITestFabricPre implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        EffectAPITest.init(new EffectAPITestHelperFabric());
    }
}
