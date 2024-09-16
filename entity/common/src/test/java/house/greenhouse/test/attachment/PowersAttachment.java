package house.greenhouse.test.attachment;

import com.mojang.serialization.Codec;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.entity.api.EntityEffectAPI;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.test.EffectAPIEntityTest;
import house.greenhouse.test.Power;
import house.greenhouse.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class PowersAttachment {
    public static final Codec<PowersAttachment> CODEC = Power.CODEC.listOf().xmap(holders -> {
        PowersAttachment attachment = new PowersAttachment();
        for (Holder<Power> power : holders)
            attachment.addDelegatedPower(power);
        return attachment;
    }, attachment -> attachment.powers);

    private List<Holder<Power>> powers = new ArrayList<>();
    private final List<Holder<Power>> delegatedPowers = new ArrayList<>();

    private Entity provider;

    public PowersAttachment() {}

    public void init(Entity entity) {
        if (provider != null)
            return;
        provider = entity;
        for (Holder<Power> power : delegatedPowers) {
            powers.add(power);
            EntityEffectAPI.addEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof VariableHolder).flatMap(component -> ((List<VariableHolder<EffectAPIEffect>>)component.value()).stream()).toList(), createSource(power));
        }
        delegatedPowers.clear();
        sync();
    }

    public List<Holder<Power>> getPowers() {
        return powers;
    }

    public boolean isEmpty() {
        return powers.isEmpty();
    }

    public int totalPowers() {
        return powers.size();
    }

    public boolean hasPower(Holder<Power> power) {
        return powers.contains(power);
    }

    public void addPower(Holder<Power> power) {
        powers.add(power);
        EntityEffectAPI.addEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof VariableHolder).flatMap(component -> ((List<VariableHolder<EffectAPIEffect>>)component.value()).stream()).toList(), createSource(power));
    }

    private void addDelegatedPower(Holder<Power> power) {
        delegatedPowers.add(power);
    }

    public void removePower(Holder<Power> power) {
        powers.remove(power);
        EntityEffectAPI.removeEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof VariableHolder).flatMap(component -> ((List<VariableHolder<EffectAPIEffect>>)component.value()).stream()).toList(), createSource(power));
    }

    public void sync() {
        if (provider.level().isClientSide())
            return;
        EntityEffectAPI.syncEffects(provider);
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(provider.getId(), powers), provider, true);
    }

    public void setFromNetwork(List<Holder<Power>> powers) {
        this.powers = powers;
    }

    public static ResourceLocation createSource(Holder<Power> holder) {
        return EffectAPIEntityTest.asResource(holder.unwrapKey().map(key -> {
            String namespace = key.location().getNamespace();
            String path = key.location().getPath();
            return "power/" + namespace + "/" + path;
        }).orElseThrow());
    }
}