
package house.greenhouse.effectapi.entity.impl.registry;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EffectsAttachment<Entity>> ENTITY_EFFECTS = AttachmentType
            .<EffectsAttachment<Entity>>builder(() -> new EffectsAttachment<>(
                    (provider, effect, source) -> EntityEffectAPI.createEntityOnlyContext(provider, source),
                    (provider, sourcesToComponents, activeComponents) -> new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), sourcesToComponents, activeComponents),
                    EffectAPIEntityLootContextParamSets.ENTITY
            )).build();

    public static void registerAll(RegistrationCallback<AttachmentType<?>> callback) {
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EffectAPIEntity.EFFECTS_ATTACHMENT_KEY, ENTITY_EFFECTS);
    }
}
