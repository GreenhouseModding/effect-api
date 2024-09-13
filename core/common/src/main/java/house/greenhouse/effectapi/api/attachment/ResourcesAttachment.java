package house.greenhouse.effectapi.api.attachment;

import net.minecraft.resources.ResourceLocation;

public interface ResourcesAttachment {
    boolean isEmpty();

    boolean hasResource(ResourceLocation id);

    <T> T getValue(ResourceLocation id);

    <T> T setValue(ResourceLocation id, T value, ResourceLocation source);

    void removeValue(ResourceLocation id, ResourceLocation source);
}
