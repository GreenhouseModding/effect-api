package dev.greenhouseteam.test.attachment;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPITickEffect;
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
            attachment.addPowerInternal(power);
        attachment.setAllComponents();
        return attachment;
    }, attachment -> attachment.powers);

    private List<Holder<Power>> powers = new ArrayList<>();
    private DataComponentMap allComponents = DataComponentMap.EMPTY;;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;
    private Entity entity;

    public PowersAttachment() {}

    public void init(Entity entity) {
        this.entity = entity;
    }

    public void getPowers(DataComponentType<?> type) {
        activeComponents.getOrDefault(type, List.of());
    }

    public int totalPowers() {
        return powers.size();
    }

    public boolean hasPower(Holder<Power> power) {
        return powers.contains(power);
    }

    public void addPower(Holder<Power> power) {
        addPowerInternal(power);
        setAllComponents();
        updateAndSync();
    }

    private void addPowerInternal(Holder<Power> power) {
        powers.add(power);
    }

    public void removePower(Holder<Power> power) {
        powers.remove(power);
        setAllComponents();
        updateAndSync();
    }

    public void tick() {
        updateAndSync();
        for (var entry : activeComponents) {
            if (entry.type() == EffectAPIEffectTypes.TICK && entry.value() instanceof List list)
                list.forEach(effect -> ((EffectAPITickEffect)((EffectAPIConditionalEffect)effect).effect()).tick(EffectAPIEffect.createEntityOnlyContext(entity)));
        }
    }

    public void refresh() {
        for (var entry : activeComponents) {
            if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> ((EffectAPIEffect)effect).onRemoved(EffectAPIEffect.createEntityOnlyContext(entity)));
        }
    }

    private void updateAndSync() {
        DataComponentMap previous = activeComponents;
        DataComponentMap potential = EffectUtil.getActive(entity, allComponents);
        if (EffectUtil.handleChangedActives(entity, potential, previous)) {
            activeComponents = potential;
            EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(entity.getId(), powers, activeComponents), entity);
        }
    }

    public void sync() {
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(entity.getId(), powers, activeComponents), entity);
    }

    private void setAllComponents() {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();

        for (DataComponentMap component : powers.stream().map(power -> power.value().effects()).toList()) {
            for (var entry : component) {
                if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                    newMap.computeIfAbsent(entry.type(), type -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) list);
                else
                    EffectAPI.LOG.warn("Attempted to add non Effect API effect to power attachment");
            }
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());
        allComponents = builder.build();
    }

    public void setFromNetwork(List<Holder<Power>> powers, DataComponentMap activeComponents) {
        this.powers = powers;
        setAllComponents();
        this.activeComponents = activeComponents;
    }
}
