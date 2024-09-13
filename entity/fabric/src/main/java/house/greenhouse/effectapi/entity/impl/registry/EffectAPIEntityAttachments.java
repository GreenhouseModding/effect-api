
package house.greenhouse.effectapi.entity.impl.registry;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.Entity;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EffectsAttachment<Entity>> ENTITY_EFFECTS = AttachmentRegistry.<EffectsAttachment<Entity>>builder()
            .initializer(() -> new EffectsAttachment<>(
                    (provider, effect, source) -> EntityEffectAPI.createEntityOnlyContext(provider, source),
                    (provider, sourcesToComponents, activeComponents) -> new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), sourcesToComponents, activeComponents),
                    EffectAPIEntityLootContextParamSets.ENTITY
            ))
            .buildAndRegister(EffectAPIEntity.EFFECTS_ATTACHMENT_KEY);

    public static void init() {

    }
}
