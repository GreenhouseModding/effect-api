package house.greenhouse.test;

import house.greenhouse.test.platform.EffectAPITestHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class EffectAPITest {
    public static final String MOD_ID = "effect_api_test";

    private static EffectAPITestHelper helper;

    public static final ResourceKey<Registry<Power>> POWER = ResourceKey.createRegistryKey(EffectAPITest.asResource("power"));

    public static void init(EffectAPITestHelper helper) {
        EffectAPITest.helper = helper;
    }

    public static EffectAPITestHelper getHelper() {
        return helper;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
