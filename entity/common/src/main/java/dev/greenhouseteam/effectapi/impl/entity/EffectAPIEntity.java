package dev.greenhouseteam.effectapi.impl.entity;

import dev.greenhouseteam.effectapi.platform.EffectAPIEntityPlatformHelper;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EffectAPIEntity {
    public static final String MOD_ID = "effectapi";
    public static final String MOD_NAME = "Effect API";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private static EffectAPIEntityPlatformHelper helper;

    public static void init(EffectAPIEntityPlatformHelper helper) {
        EffectAPIEntity.helper = helper;
    }

    public static EffectAPIEntityPlatformHelper getHelper() {
        return helper;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}