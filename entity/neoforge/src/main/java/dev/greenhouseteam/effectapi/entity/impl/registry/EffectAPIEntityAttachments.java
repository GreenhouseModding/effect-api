
package dev.greenhouseteam.effectapi.entity.impl.registry;

import dev.greenhouseteam.effectapi.entity.api.attachment.EntityEffectsAttachment;
import dev.greenhouseteam.effectapi.impl.registry.internal.RegistrationCallback;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EntityEffectsAttachment> ENTITY_EFFECTS = AttachmentType
            .builder(EntityEffectsAttachment::new)
            .copyOnDeath()
            .build();

    public static void registerAll(RegistrationCallback<AttachmentType<?>> callback) {
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EntityEffectsAttachment.ID, ENTITY_EFFECTS);
    }
}
