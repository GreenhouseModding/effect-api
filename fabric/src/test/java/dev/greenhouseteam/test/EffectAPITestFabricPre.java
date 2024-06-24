package dev.greenhouseteam.test;

import dev.greenhouseteam.test.platform.EffectAPITestHelperFabric;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class EffectAPITestFabricPre implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        EffectAPITest.init(new EffectAPITestHelperFabric());
    }
}
