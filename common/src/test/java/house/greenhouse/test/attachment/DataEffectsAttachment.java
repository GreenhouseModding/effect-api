package house.greenhouse.test.attachment;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.EntityEffectAPI;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.test.DataEffect;
import house.greenhouse.test.EffectAPITest;
import house.greenhouse.test.network.clientbound.SyncDataEffectAttachmentClientboundPacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class DataEffectsAttachment {
    public static final Codec<DataEffectsAttachment> CODEC = DataEffect.CODEC.listOf().xmap(holders -> {
        DataEffectsAttachment attachment = new DataEffectsAttachment();
        for (Holder<DataEffect> power : holders)
            attachment.addDelegatedEffect(power);
        return attachment;
    }, attachment -> attachment.effects);

    private List<Holder<DataEffect>> effects = new ArrayList<>();
    private final List<Holder<DataEffect>> delegatedEffects = new ArrayList<>();

    private Entity provider;

    public DataEffectsAttachment() {}

    public void init(Entity entity) {
        if (provider != null)
            return;
        provider = entity;
        for (Holder<DataEffect> power : delegatedEffects) {
            effects.add(power);
            EntityEffectAPI.addEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof EffectHolder).flatMap(component -> ((List<EffectHolder<EffectAPIEffect>>)component.value()).stream()).toList(), createSource(power));
        }
        delegatedEffects.clear();
        sync();
    }

    public List<Holder<DataEffect>> getPowers() {
        return effects;
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }

    public int totalPowers() {
        return effects.size();
    }

    public boolean hasEffect(Holder<DataEffect> power) {
        return effects.contains(power);
    }

    public void addEffect(Holder<DataEffect> power) {
        effects.add(power);
        EntityEffectAPI.addEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof EffectHolder).flatMap(component -> ((List<EffectHolder<EffectAPIEffect>>)component.value()).stream()).toList(), createSource(power));
    }

    private void addDelegatedEffect(Holder<DataEffect> power) {
        delegatedEffects.add(power);
    }

    public void removeEffect(Holder<DataEffect> power) {
        effects.remove(power);
        EntityEffectAPI.removeEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof EffectHolder).flatMap(component -> ((List<EffectHolder<EffectAPIEffect>>)component.value()).stream()).toList(), createSource(power));
    }

    public void sync() {
        if (provider.level().isClientSide())
            return;
        EntityEffectAPI.syncEffects(provider);
        EffectAPI.getHelper().sendClientboundTracking(new SyncDataEffectAttachmentClientboundPacket(provider.getId(), effects), provider, true);
    }

    public void setFromNetwork(List<Holder<DataEffect>> powers) {
        this.effects = powers;
    }

    public static ResourceLocation createSource(Holder<DataEffect> holder) {
        return EffectAPITest.asResource(holder.unwrapKey().map(key -> {
            String namespace = key.location().getNamespace();
            String path = key.location().getPath();
            return "power/" + namespace + "/" + path;
        }).orElseThrow());
    }
}