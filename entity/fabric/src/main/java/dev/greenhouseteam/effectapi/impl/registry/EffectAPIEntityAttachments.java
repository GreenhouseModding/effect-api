
package dev.greenhouseteam.effectapi.impl.registry;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.api.entity.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

import java.util.HashMap;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<ResourcesAttachment> RESOURCES = AttachmentRegistry.<ResourcesAttachment>builder()
            .initializer(() -> new ResourcesAttachment(new HashMap<>()))
            .persistent(ResourcesAttachment.CODEC)
            .copyOnDeath()
            .buildAndRegister(EffectAPI.asResource("entity_resources"));
    public static final AttachmentType<EntityEffectsAttachment> EFFECTS = AttachmentRegistry.<EntityEffectsAttachment>builder()
            .initializer(EntityEffectsAttachment::new)
            .copyOnDeath()
            .buildAndRegister(EntityEffectsAttachment.ID);

    public static void init() {

    }
}
