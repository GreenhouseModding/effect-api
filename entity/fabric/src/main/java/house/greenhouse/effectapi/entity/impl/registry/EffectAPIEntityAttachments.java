
package house.greenhouse.effectapi.entity.impl.registry;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.EffectAPI;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EffectsAttachmentImpl<Entity>> ENTITY_EFFECTS = AttachmentRegistry.<EffectsAttachmentImpl<Entity>>builder()
            .initializer(() -> new EffectsAttachmentImpl<>(
                    (provider, effect, source) -> EntityEffectAPI.createEntityOnlyContext(provider, source),
                    (provider, sourcesToComponents, activeComponents, receiver) -> ServerPlayNetworking.send(receiver, new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), sourcesToComponents, activeComponents)),
                    (provider, sourcesToComponents, activeComponents) -> EffectAPI.getHelper().sendClientboundTracking(new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), sourcesToComponents, activeComponents), provider),
                    EffectAPIEntityLootContextParamSets.ENTITY
            ))
            .buildAndRegister(EffectAPIEntity.EFFECTS_ATTACHMENT_KEY);

    public static void init() {

    }
}
