package house.greenhouse.effectapi.impl.registry;

import house.greenhouse.effectapi.api.predicate.EntityResourceCondition;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class EffectAPIPredicates {
    public static final LootItemConditionType ENTITY_RESOURCE = new LootItemConditionType(EntityResourceCondition.CODEC);

    public static void registerAll(RegistrationCallback<LootItemConditionType> callback) {
        callback.register(BuiltInRegistries.LOOT_CONDITION_TYPE, EffectAPI.asResource("entity_resource"), ENTITY_RESOURCE);
    }
}
