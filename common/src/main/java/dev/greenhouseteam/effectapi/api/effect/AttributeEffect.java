package dev.greenhouseteam.effectapi.api.effect;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.greenhouseteam.effectapi.api.EffectAPIEffectTypes;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParamSets;
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
    public void onAdded(LootContext lootContext) {
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
            living.getAttributes().addTransientAttributeModifiers(this.makeAttributeMap(lootContext));
    }

    @Override
    public void onRemoved(LootContext lootContext) {
        if (lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
            living.getAttributes().removeAttributeModifiers(this.makeAttributeMap(lootContext));
    }

    private HashMultimap<Holder<Attribute>, AttributeModifier> makeAttributeMap(LootContext context) {
        HashMultimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        map.put(this.attribute, new AttributeModifier(id, amount.getFloat(context), operation));
        return map;
    }

    @Override
    public DataComponentType<?> type() {
        return EffectAPIEffectTypes.ATTRIBUTE;
    }

    @Override
    public LootContextParamSet paramSet() {
        return EffectAPILootContextParamSets.ENTITY;
    }
}
