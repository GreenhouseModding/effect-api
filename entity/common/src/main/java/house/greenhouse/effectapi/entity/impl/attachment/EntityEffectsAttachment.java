package house.greenhouse.effectapi.entity.impl.attachment;

import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
import house.greenhouse.effectapi.entity.impl.network.clientbound.SyncEntityEffectsAttachmentClientboundPacket;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class EntityEffectsAttachment extends EffectsAttachmentImpl<Entity> {
    @Override
    public int getTickCount() {
        return provider.tickCount;
    }

    @Override
    public void tick() {
        if (provider == null || !provider.isAlive())
            return;
        super.tick();
    }

    @Override
    protected void syncInternal(ServerPlayer player) {
        EffectAPI.getHelper().sendClientbound(new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), combinedComponents, activeComponents), player);
    }

    @Override
    protected void syncInternal() {
        EffectAPI.getHelper().sendClientboundTracking(new SyncEntityEffectsAttachmentClientboundPacket(provider.getId(), combinedComponents, activeComponents), provider);
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
