
package house.greenhouse.effectapi.entity.impl.registry;

import house.greenhouse.effectapi.entity.impl.attachment.EntityEffectsAttachment;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EntityEffectsAttachment> ENTITY_EFFECTS = AttachmentRegistry.<EntityEffectsAttachment>builder()
            .initializer(EntityEffectsAttachment::new)
            .buildAndRegister(EffectAPIEntity.EFFECTS_ATTACHMENT_KEY);

    public static void init() {

    }
}
