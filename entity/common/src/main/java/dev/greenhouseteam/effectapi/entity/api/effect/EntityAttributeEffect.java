package dev.greenhouseteam.effectapi.entity.api.effect;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.entity.api.EffectAPIEntityEffectTypes;
import dev.greenhouseteam.effectapi.entity.api.registry.EffectAPIEntityLootContextParamSets;
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

public record EntityAttributeEffect(ResourceLocation id, Holder<Attribute> attribute, NumberProvider amount, AttributeModifier.Operation operation) implements EffectAPIEffect {
    public static final Codec<EntityAttributeEffect> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(EntityAttributeEffect::id),
            Attribute.CODEC.fieldOf("attribute").forGetter(EntityAttributeEffect::attribute),
            NumberProviders.CODEC.fieldOf("amount").forGetter(EntityAttributeEffect::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(EntityAttributeEffect::operation)
    ).apply(inst, EntityAttributeEffect::new));

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
        return EffectAPIEntityEffectTypes.ENTITY_ATTRIBUTE;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPIEntityLootContextParamSets.ENTITY;
    }
}
