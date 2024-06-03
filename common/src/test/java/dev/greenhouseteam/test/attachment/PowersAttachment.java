package dev.greenhouseteam.test.attachment;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.attachment.EffectSource;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPITickingEffect;
import dev.greenhouseteam.effectapi.api.util.EffectUtil;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.test.EffectAPITest;
import dev.greenhouseteam.test.Power;
import dev.greenhouseteam.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PowersAttachment {
    public static final ResourceLocation ID = EffectAPITest.asResource("powers");
    public static final Codec<PowersAttachment> CODEC = Power.CODEC.listOf().xmap(holders -> {
        PowersAttachment attachment = new PowersAttachment();
        for (Holder<Power> power : holders)
            attachment.addDelegatedPower(power);
        return attachment;
    }, attachment -> attachment.powers);

    private List<Holder<Power>> powers = new ArrayList<>();
    private List<Holder<Power>> powersToApply = new ArrayList<>();
    private Entity entity;

    public PowersAttachment() {}

    public void init(Entity entity) {
        this.entity = entity;
        if (EffectAPI.getHelper().getEffects(entity) != null) {
            EffectAPI.getHelper().getEffects(entity).init(entity);
        }
        powersToApply.forEach(p -> {
            for (var entry : p.value().effects())
                for (EffectAPIEffect effect : (List<EffectAPIEffect>) entry.value())
                    EffectAPI.getHelper().addEffect(entity, effect, new EffectSource(ID, true));
            if (entity.level().isClientSide())
                return;
        });
        powersToApply.clear();
    }


    public int totalPowers() {
        return powers.size();
    }

    public boolean hasPower(Holder<Power> power) {
        return powers.contains(power);
    }

    public void addPower(Holder<Power> power) {
        powers.add(power);
        for (var entry : power.value().effects())
            for (EffectAPIEffect effect : (List<EffectAPIEffect>) entry.value())
                EffectAPI.getHelper().addEffect(entity, effect, new EffectSource(ID, true));
        if (entity.level().isClientSide())
            return;
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(entity.getId(), List.of(power), false), entity);
    }

    private void addDelegatedPower(Holder<Power> power) {
        powers.add(power);
        powersToApply.add(power);
    }

    public void removePower(Holder<Power> power) {
        powers.remove(power);
        for (var entry : power.value().effects())
            for (EffectAPIEffect effect : (List<EffectAPIEffect>) entry.value())
                EffectAPI.getHelper().removeEffect(entity, effect, new EffectSource(ID, true));
        if (entity.level().isClientSide())
            return;
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(entity.getId(), List.of(power), true), entity);
    }

    public void sync() {
        if (entity.level().isClientSide())
            return;
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(entity.getId(), powers, false), entity);
    }

    public void addFromNetwork(List<Holder<Power>> powers, boolean remove) {
        if (remove) {
            powers.forEach(power -> removePower(power));
            return;
        }
        powers.forEach(power -> addPower(power));
    }
}
