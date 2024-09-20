package house.greenhouse.test;

import house.greenhouse.test.platform.EffectAPITestHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class EffectAPITest {
    public static final String MOD_ID = "effect_api_test";
    public static final ResourceLocation DATA_EFFECTS_ATTACHMENT_KEY = asResource("data_effects");

    private static EffectAPITestHelper helper;

    public static final ResourceKey<Registry<DataEffect>> DATA_EFFECT = ResourceKey.createRegistryKey(EffectAPITest.asResource("effect"));

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
