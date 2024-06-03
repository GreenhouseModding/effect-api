
package dev.greenhouseteam.effectapi.impl.registry;

import dev.greenhouseteam.effectapi.api.attachment.EffectsAttachment;
import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import java.util.ArrayList;
import java.util.HashMap;

public class EffectAPIAttachments {
    public static final AttachmentType<EffectsAttachment> EFFECTS = AttachmentRegistry.<EffectsAttachment>builder()
            .initializer(EffectsAttachment::new)
            .persistent(EffectsAttachment.CODEC)
            .buildAndRegister(EffectsAttachment.ID);
    public static final AttachmentType<ResourcesAttachment> RESOURCES = AttachmentRegistry.<ResourcesAttachment>builder()
            .initializer(() -> new ResourcesAttachment(new HashMap<>()))
            .persistent(ResourcesAttachment.CODEC)
            .buildAndRegister(ResourcesAttachment.ID);

    public static void init() {

    }
}
