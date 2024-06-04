package dev.greenhouseteam.test.attachment;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.util.EffectEntityUtil;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.test.EffectAPITest;
import dev.greenhouseteam.test.Power;
import dev.greenhouseteam.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class PowersAttachment {
    public static final ResourceLocation ID = EffectAPITest.asResource("powers");
    public static final Codec<PowersAttachment> CODEC = Power.CODEC.listOf().xmap(holders -> {
        PowersAttachment attachment = new PowersAttachment();
        for (Holder<Power> power : holders)
            attachment.addDelegatedPower(power);
        return attachment;
    }, attachment -> attachment.powers);

    private List<Holder<Power>> powers = new ArrayList<>();
    private List<Holder<Power>> delegatedPowers = new ArrayList<>();

    private Entity provider;

    public PowersAttachment() {}

    public void init(Entity entity) {
        if (provider != null)
            return;
        provider = entity;
        for (Holder<Power> power : delegatedPowers)
            addPower(power);
        delegatedPowers.clear();
        sync();
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
        EffectEntityUtil.addEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect).flatMap(component -> ((List<EffectAPIEffect>)component.value()).stream()).toList(), ID);
        powers.add(power);
        EffectEntityUtil.syncEffects(provider);
        sync();
    }

    private void addDelegatedPower(Holder<Power> power) {
        delegatedPowers.add(power);
    }

    public void removePower(Holder<Power> power) {
        EffectEntityUtil.removeEffects(provider, power.value().effects().stream().filter(component -> component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect).flatMap(component -> ((List<EffectAPIEffect>)component.value()).stream()).toList(), ID);
        powers.remove(power);
        EffectEntityUtil.syncEffects(provider);
        sync();
    }

    public void sync() {
        if (provider.level().isClientSide())
            return;
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(provider.getId(), powers), provider);
    }

    public void setFromNetwork(List<Holder<Power>> powers) {
        this.powers = powers;
    }
}
