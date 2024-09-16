package house.greenhouse.effectapi.entity.api.predicate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.entity.api.EntityResourceAPI;
import house.greenhouse.effectapi.entity.api.registry.EffectAPIEntityPredicates;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Optional;
import java.util.stream.Stream;

public record EntityResourceCondition(Holder<Resource<Object>> resource, Object value, LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final MapCodec<EntityResourceCondition> CODEC = new EntityResourceConditionCodec();

    @Override
    public boolean test(LootContext context) {
        if (!context.hasParam(entityTarget.getParam()))
            return false;
        Entity entity = context.getParam(entityTarget.getParam());
        Object compareTo = EntityResourceAPI.getResourceValue(entity, resource);
        return value.equals(compareTo);
    }

    @Override
    public LootItemConditionType getType() {
        return EffectAPIEntityPredicates.ENTITY_RESOURCCE;
    }

    public static class EntityResourceConditionCodec extends MapCodec<EntityResourceCondition> {
        @Override
        public <T> DataResult<EntityResourceCondition> decode(DynamicOps<T> ops, MapLike<T> mapLike) {
            if (!(ops instanceof RegistryOps<T> registryOps))
                return DataResult.error(() -> "Cannot decode resources attachment from a non registry context.");
            DataResult<Pair<ResourceLocation, T>> id = ResourceLocation.CODEC.decode(ops, mapLike.get("id"));
            if (id.isError()) {
                return DataResult.error(() -> "Failed to parse 'id' field for entity resource condition: " + id.error().get().message());
            }

            Optional<Holder.Reference<Resource<?>>> effect = registryOps.getter(EffectAPIRegistryKeys.RESOURCE).orElseThrow().get(ResourceKey.create(EffectAPIRegistryKeys.RESOURCE, id.getOrThrow().getFirst()));

            if (effect.isEmpty())
                return DataResult.error(() -> "Could not find resource effect with id '" + id.getOrThrow() + "'.");

            var newValue = effect.get().value().dataType().codec().decode(ops, mapLike.get("value"));
            if (newValue.isError())
                return DataResult.error(() -> "Failed to decode value for entity resource condition for: '" + id.getOrThrow() + "'." + newValue.error().get().message());
            var value = newValue.getOrThrow().getFirst();

            LootContext.EntityTarget target = LootContext.EntityTarget.CODEC.decode(ops, mapLike.get("target")).getOrThrow().getFirst();

            return DataResult.success(new EntityResourceCondition((Holder)effect.get(), value, target));
        }

        @Override
        public <T> RecordBuilder<T> encode(EntityResourceCondition input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            prefix.add("id", ops.createString(input.resource.unwrapKey().get().location().toString()));
            prefix.add("value", input.resource.value().dataType().codec().encodeStart(ops, input.value));
            prefix.add("target", LootContext.EntityTarget.CODEC.encodeStart(ops, input.entityTarget));
            return prefix;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("id"), ops.createString("value"), ops.createString("target"));
        }
    }
}
