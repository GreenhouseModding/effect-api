package house.greenhouse.test;

import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.test.platform.EffectAPITestHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class EffectAPIEntityTest {
    public static final String MOD_ID = "effect_api_entity_test";
    public static final ResourceLocation POWERS_ATTACHMENT_KEY = asResource("powers");

    private static EffectAPITestHelper helper;

    public static final ResourceKey<Registry<Power>> POWER = ResourceKey.createRegistryKey(EffectAPIEntityTest.asResource("power"));

    public static void init(EffectAPITestHelper helper) {
        EffectAPIEntityTest.helper = helper;
    }

    public static EffectAPITestHelper getHelper() {
        return helper;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
