package house.greenhouse.effectapi.impl.util;

import house.greenhouse.effectapi.api.effect.ResourceEffect;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class InternalResourceUtil {
    private static final Map<ResourceLocation, ResourceEffect<?>> ID_TO_EFFECT_MAP = new HashMap<>();

    public static void putInEffectMap(ResourceLocation id, ResourceEffect<?> effect) {
        ID_TO_EFFECT_MAP.put(id, effect);
    }

    @Nullable
    public static <T> ResourceEffect<T> getEffectFromId(ResourceLocation id) {
        return (ResourceEffect<T>) ID_TO_EFFECT_MAP.get(id);
    }

    public static Map<ResourceLocation, ResourceEffect<?>> getIdMap() {
        return Map.copyOf(ID_TO_EFFECT_MAP);
    }

    public static void clearEffectMap() {
        ID_TO_EFFECT_MAP.clear();
    }
}
