
package house.greenhouse.effectapi.entity.impl.registry;

import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class EffectAPIEntityAttachments {
    public static final AttachmentType<EffectsAttachmentImpl<Entity>> ENTITY_EFFECTS = AttachmentType
            .<EffectsAttachmentImpl<Entity>>builder(() -> new EffectsAttachmentImpl<>(
                    (provider, effect, source) -> EntityEffectAPI.createEntityOnlyContext(provider, source),
                    (provider, sourcesToComponents, activeComponents, receiver) -> PacketDistributor.sendToPlayer(receiver, new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), sourcesToComponents, activeComponents)),
                    (provider, sourcesToComponents, activeComponents) -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(provider, new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), sourcesToComponents, activeComponents)),
                    EffectAPIEntityLootContextParamSets.ENTITY
            )).build();

    public static void registerAll(RegistrationCallback<AttachmentType<?>> callback) {
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EffectAPIEntity.EFFECTS_ATTACHMENT_KEY, ENTITY_EFFECTS);
    }
}
