package house.greenhouse.effectapi.entity.impl.attachment;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.variable.EffectHolder;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
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
    public <E extends EffectAPIEffect> LootContext createLootContext(EffectHolder<E> holder, ResourceLocation source) {
        return EffectAPIEntityEffectTypes.buildContext(holder.effectType(), provider, source);
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
