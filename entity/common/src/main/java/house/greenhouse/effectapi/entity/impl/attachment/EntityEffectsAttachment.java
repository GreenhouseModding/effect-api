package house.greenhouse.effectapi.entity.impl.attachment;

import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class EntityEffectsAttachment extends EffectsAttachmentImpl<Entity> {
    @Override
    public int getTickCount() {
        return provider.tickCount;
    }

    @Override
    public LootContext createLootContext(ResourceLocation source) {
        return EntityEffectAPI.createEntityOnlyContext(provider, source);
    }

    @Override
    public void sync(ServerPlayer player) {
        EffectAPI.getHelper().sendClientbound(new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), combinedComponents, activeComponents), player);
    }

    @Override
    public void sync() {
        EffectAPI.getHelper().sendClientboundTracking(new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), combinedComponents, activeComponents), provider);
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
