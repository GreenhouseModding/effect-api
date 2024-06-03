package dev.greenhouseteam.test.attachment;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPITickingEffect;
import dev.greenhouseteam.effectapi.api.util.EffectUtil;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.test.EffectAPITest;
import dev.greenhouseteam.test.Power;
import dev.greenhouseteam.test.network.clientbound.SyncPowerAttachmentClientboundPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class PowersAttachment {
    public static final ResourceLocation ID = EffectAPITest.asResource("powers");
    public static final Codec<PowersAttachment> CODEC = Power.CODEC.listOf().xmap(holders -> {
        PowersAttachment attachment = new PowersAttachment();
        for (Holder<Power> power : holders)
            attachment.addPower(power, false);
        return attachment;
    }, attachment -> attachment.powers);

    private List<Holder<Power>> powers = new ArrayList<>();

    private DataComponentMap allComponents = DataComponentMap.EMPTY;
    private DataComponentMap activeComponents = DataComponentMap.EMPTY;

    private Entity provider;

    public PowersAttachment() {}

    public void init(Entity entity) {
        this.provider = entity;
    }

    public boolean isEmpty() {
        return powers.isEmpty();
    }

    public void getEffects(DataComponentType<?> type) {
        activeComponents.getOrDefault(type, List.of());
    }

    public int totalPowers() {
        return powers.size();
    }

    public boolean hasPower(Holder<Power> power) {
        return powers.contains(power);
    }


    public void tick() {
        updateActiveComponents();
        for (var entry : activeComponents) {
            if (entry.type() == EffectAPIEffectTypes.ENTITY_TICK && entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> ((EffectAPITickingEffect)((EffectAPIConditionalEffect)effect).effect()).tick(EffectAPIEffect.createEntityOnlyContext(provider)));
        }
    }

    public void refresh() {
        for (var entry : activeComponents) {
            if (entry.type() == EffectAPIEffectTypes.ENTITY_TICK && entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> ((EffectAPITickingEffect)((EffectAPIConditionalEffect)effect).effect()).onRefreshed(EffectAPIEffect.createEntityOnlyContext(provider)));
        }
    }

    private void updateActiveComponents() {
        DataComponentMap previous = activeComponents;
        DataComponentMap potential = EffectUtil.getActive(provider, allComponents);
        if (EffectUtil.hasUpdatedActives(provider, potential, previous)) {
            activeComponents = potential;
            sync();
        }
    }

    public void addPower(Holder<Power> power) {
        addPower(power, true);
    }

    private void addPower(Holder<Power> power, boolean shouldSync) {
        powers.add(power);
        recalculateComponents();
        if (!shouldSync)
            return;
        sync();
    }


    public void removePower(Holder<Power> power) {
        powers.remove(power);
        recalculateComponents();
        sync();
    }

    public void sync() {
        if (provider.level().isClientSide())
            return;
        EffectAPI.getHelper().sendClientboundTracking(new SyncPowerAttachmentClientboundPacket(provider.getId(), powers), provider);
    }

    public void setFromNetwork(List<Holder<Power>> powers) {
        this.powers = powers;
        recalculateComponents();
    }

    public void recalculateComponents() {
        Map<DataComponentType<?>, List<EffectAPIEffect>> map = new HashMap<>();
        for (Holder<Power> power : powers) {
            for (var value : power.value().effects())
                map.computeIfAbsent(value.type(), t -> new ArrayList<>()).addAll((Collection<? extends EffectAPIEffect>) value.value());
        }
        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var value : map.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) value.getKey(), value.getValue());
        allComponents = builder.build();
    }
}
