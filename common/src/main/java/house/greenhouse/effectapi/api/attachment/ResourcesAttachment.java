package house.greenhouse.effectapi.api.attachment;

import house.greenhouse.effectapi.api.resource.Resource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

public interface ResourcesAttachment {
    boolean isEmpty();

    <T> boolean hasResource(Holder<Resource<T>> resource);
    <T> boolean hasSource(Holder<Resource<T>> resource, ResourceLocation source);
    <T> boolean hasSourceFromMod(Holder<Resource<T>> resource, String modId);

    <T> T getValue(Holder<Resource<T>> resource);

    <T> T setValue(Holder<Resource<T>> resource, T value, ResourceLocation source);

    <T> void removeValue(Holder<Resource<T>> resource, ResourceLocation source);
}