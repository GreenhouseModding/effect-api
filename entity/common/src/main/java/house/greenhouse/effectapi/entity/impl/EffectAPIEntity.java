package house.greenhouse.effectapi.entity.impl;

import house.greenhouse.effectapi.entity.platform.EffectAPIEntityPlatformHelper;

public class EffectAPIEntity {
    private static EffectAPIEntityPlatformHelper helper;

    public static void init(EffectAPIEntityPlatformHelper helper) {
        EffectAPIEntity.helper = helper;
    }

    public static EffectAPIEntityPlatformHelper getHelper() {
        return helper;
    }
}