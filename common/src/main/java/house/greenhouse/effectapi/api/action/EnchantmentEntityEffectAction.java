package house.greenhouse.effectapi.api.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import house.greenhouse.effectapi.api.EffectAPIActionTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record EnchantmentEntityEffectAction<T extends EnchantmentEntityEffect>(T effect, Optional<EquipmentSlot> slot) implements EffectAPIAction {
    public static final MapCodec<EnchantmentEntityEffectAction<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            EnchantmentEntityEffect.CODEC.fieldOf("effect").forGetter(EnchantmentEntityEffectAction::effect),
            EquipmentSlot.CODEC.optionalFieldOf("slot").forGetter(EnchantmentEntityEffectAction::slot)
    ).apply(inst, EnchantmentEntityEffectAction::new));
    public static final ActionType<EnchantmentEntityEffectAction<?>> TYPE = new ActionType<>(paramSet -> CODEC);

    @Override
    public void apply(LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity living) || living.level().isClientSide())
            return;
        int level = Optional.ofNullable(context.getParamOrNull(LootContextParams.ENCHANTMENT_LEVEL)).orElse(1);
        ItemStack stack = Optional.ofNullable(context.getParamOrNull(LootContextParams.TOOL)).orElseGet(() -> slot().map(living::getItemBySlot).orElse(ItemStack.EMPTY));
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
    public Collection<LootContextParam<?>> requiredParams() {
        return List.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public ActionType<?> type() {
        return TYPE;
    }
}
