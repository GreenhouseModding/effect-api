package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.platform.EffectAPIPlatformHelper;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectAPI {
    public static final String MOD_ID = "effectapi";
    public static final String MOD_NAME = "Effect API";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static EffectAPIPlatformHelper helper;

    public static void init(EffectAPIPlatformHelper helper) {
        EffectAPI.helper = helper;
    }

    public static EffectAPIPlatformHelper getHelper() {
        return helper;
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}