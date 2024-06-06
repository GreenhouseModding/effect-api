package dev.greenhouseteam.effectapi.api.entity.effect;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.entity.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.api.entity.registry.EffectAPILootContextParamSets;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record AttributeEffect(ResourceLocation id, Holder<Attribute> attribute, NumberProvider amount, AttributeModifier.Operation operation) implements EffectAPIEffect {
    public static final Codec<AttributeEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(AttributeEffect::id),
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeEffect::attribute),
            NumberProviders.CODEC.fieldOf("amount").forGetter(AttributeEffect::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeEffect::operation)
    ).apply(inst, AttributeEffect::new));

    @Override
    public void onAdded(LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
            living.getAttributes().addTransientAttributeModifiers(this.makeAttributeMap(context));
    }

    @Override
    public void onRemoved(LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
            living.getAttributes().removeAttributeModifiers(this.makeAttributeMap(context));
    }

    private HashMultimap<Holder<Attribute>, AttributeModifier> makeAttributeMap(LootContext context) {
        HashMultimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        map.put(this.attribute, new AttributeModifier(id, amount.getFloat(context), operation));
        return map;
    }

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEntityEffectTypes.ATTRIBUTE;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPILootContextParamSets.ENTITY;
    }
}
