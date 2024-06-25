
package dev.greenhouseteam.effectapi.entity.impl.registry;

import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.entity.api.entity.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;

public class EffectAPIAttachments {
    public static final AttachmentType<ResourcesAttachment> RESOURCES = AttachmentType
            .builder(() -> new ResourcesAttachment(new HashMap<>()))
            .serialize(ResourcesAttachment.CODEC)
            .copyOnDeath()
            .build();
    public static final AttachmentType<EntityEffectsAttachment> EFFECTS = AttachmentType
            .builder(EntityEffectsAttachment::new)
            .copyOnDeath()
            .build();

    public static void registerAll(RegistrationCallback<AttachmentType<?>> callback) {
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EntityEffectsAttachment.ID, RESOURCES);
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EffectAPI.asResource("entity_resources"), RESOURCES);
    }
}
