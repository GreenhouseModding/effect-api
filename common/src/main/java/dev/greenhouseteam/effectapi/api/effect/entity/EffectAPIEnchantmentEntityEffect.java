package dev.greenhouseteam.effectapi.api.effect.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public record EffectAPIEnchantmentEntityEffect<T extends EnchantmentEntityEffect>(T effect, Optional<EquipmentSlot> slot) implements EffectAPIEntityEffect {
    public static final MapCodec<EffectAPIEnchantmentEntityEffect<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            EnchantmentEntityEffect.CODEC.fieldOf("effect").forGetter(EffectAPIEnchantmentEntityEffect::effect),
            EquipmentSlot.CODEC.optionalFieldOf("slot").forGetter(EffectAPIEnchantmentEntityEffect::slot)
    ).apply(inst, EffectAPIEnchantmentEntityEffect::new));

    @Override
    public void apply(LootContext lootContext) {
        if (!(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living) || living.level().isClientSide())
            return;
        int level = Optional.ofNullable(lootContext.getParamOrNull(LootContextParams.ENCHANTMENT_LEVEL)).orElse(1);
        ItemStack stack = Optional.ofNullable(lootContext.getParamOrNull(LootContextParams.TOOL)).orElse(ItemStack.EMPTY);
        EquipmentSlot slot = slot().orElse(EquipmentSlot.MAINHAND);
        if (slot().isEmpty() && !stack.isEmpty()) {
            for (EquipmentSlot potential : EquipmentSlot.values()) {
                if (living.getItemBySlot(slot) == stack) {
                    slot = potential;
                    break;
                }
            }
        }
        effect.apply((ServerLevel) living.level(), level, new EnchantedItemInUse(stack, slot, living), living, living.position());
    }

    @Override
    public MapCodec<? extends EffectAPIEntityEffect> codec() {
        return CODEC;
    }
}
