package house.greenhouse.effectapi.impl.registry;

import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import java.util.HashMap;

public class EffectAPIAttachments {
    public static final AttachmentType<ResourcesAttachment> RESOURCES = AttachmentRegistry.<ResourcesAttachment>builder()
            .initializer(() -> new ResourcesAttachmentImpl(new HashMap<>()))
            .persistent(ResourcesAttachmentImpl.CODEC)
            .copyOnDeath()
            .buildAndRegister(EffectAPI.asResource("resources"));

    public static void init() {

    }
}