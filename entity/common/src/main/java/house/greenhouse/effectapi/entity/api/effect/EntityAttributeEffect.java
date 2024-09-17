package house.greenhouse.effectapi.entity.api.effect;

import com.google.common.collect.HashMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.loot.LootContext;
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
    public void onActivated(LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
            living.getAttributes().addTransientAttributeModifiers(makeAttributeMap(context));
    }

    @Override
    public void onDeactivated(LootContext context) {
        if (context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living)
            living.getAttributes().removeAttributeModifiers(makeAttributeMap(context));
    }

    @Override
    public void onRemoved(LootContext context) {
        onDeactivated(context);
    }

    @Override
    public void onChanged(LootContext context, EffectAPIEffect previous, boolean active) {
        previous.onDeactivated(context);
        if (active)
            onActivated(context);
    }

    private HashMultimap<Holder<Attribute>, AttributeModifier> makeAttributeMap(LootContext context) {
        HashMultimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
        map.put(this.attribute, new AttributeModifier(id, amount.getFloat(context), operation));
        return map;
    }

    @Override
    public EffectType<?, ?> type() {
        return EffectAPIEntityEffectTypes.ENTITY_ATTRIBUTE;
    }
}
