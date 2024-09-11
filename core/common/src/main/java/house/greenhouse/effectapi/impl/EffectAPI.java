package house.greenhouse.effectapi.impl;

import house.greenhouse.effectapi.platform.EffectAPIBasePlatformHelper;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectAPI {
    public static final String MOD_ID = "effect_api";
    public static final String MOD_NAME = "Effect API";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static EffectAPIBasePlatformHelper helper;

    public static void init(EffectAPIBasePlatformHelper helper) {
        EffectAPI.helper = helper;
    }

    public static EffectAPIBasePlatformHelper getHelper() {
        return helper;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}