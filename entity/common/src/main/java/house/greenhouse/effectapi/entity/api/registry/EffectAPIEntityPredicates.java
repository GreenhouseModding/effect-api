package house.greenhouse.effectapi.entity.api.registry;

import house.greenhouse.effectapi.entity.api.predicate.EntityResourceCondition;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class EffectAPIEntityPredicates {
    public static final LootItemConditionType ENTITY_RESOURCCE = new LootItemConditionType(EntityResourceCondition.CODEC);

    public static void registerAll(RegistrationCallback<LootItemConditionType> callback) {
        callback.register(BuiltInRegistries.LOOT_CONDITION_TYPE, EffectAPI.asResource("entity_resource"), ENTITY_RESOURCCE);
    }
}
