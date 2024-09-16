
package house.greenhouse.effectapi.entity.impl.registry;

import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.attachment.EntityEffectsAttachment;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EntityEffectsAttachment> ENTITY_EFFECTS = AttachmentType.builder(EntityEffectsAttachment::new).build();

    public static void registerAll(RegistrationCallback<AttachmentType<?>> callback) {
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EffectAPIEntity.EFFECTS_ATTACHMENT_KEY, ENTITY_EFFECTS);
    }
}
