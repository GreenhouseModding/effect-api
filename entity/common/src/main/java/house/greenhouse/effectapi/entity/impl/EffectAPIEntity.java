package house.greenhouse.effectapi.entity.impl;

import house.greenhouse.effectapi.entity.platform.EffectAPIEntityPlatformHelper;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.minecraft.resources.ResourceLocation;

public class EffectAPIEntity {
    public static final ResourceLocation EFFECTS_ATTACHMENT_KEY = EffectAPI.asResource("entity_effects");

    private static EffectAPIEntityPlatformHelper helper;

    public static void init(EffectAPIEntityPlatformHelper helper) {
        EffectAPIEntity.helper = helper;
    }

    public static EffectAPIEntityPlatformHelper getHelper() {
        return helper;
    }
}