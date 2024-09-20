package house.greenhouse.effectapi.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import house.greenhouse.effectapi.api.EffectAPIVariableTypes;
import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParamSets;
import house.greenhouse.effectapi.api.registry.EffectAPILootContextParams;
import house.greenhouse.effectapi.api.variable.VariableHolder;
import house.greenhouse.effectapi.impl.effect.EffectHolderImpl;
import house.greenhouse.effectapi.mixin.DataComponentMapBuilderAccessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Map;
import java.util.Optional;

public class EffectAPIEffectTypeInternals {
    public static LootContext buildDefaultContext(Entity entity, ResourceLocation source) {
        if (entity.level().isClientSide())
            return null;

        LootParams.Builder params = new LootParams.Builder((ServerLevel) entity.level());
        params.withParameter(LootContextParams.THIS_ENTITY, entity);
        params.withParameter(LootContextParams.ORIGIN, entity.position());
        params.withOptionalParameter(EffectAPILootContextParams.SOURCE, source);
        return new LootContext.Builder(params.create(EffectAPILootContextParamSets.ENTITY)).create(Optional.empty());
    }

    public static DataResult<DataComponentMap> encodeComponents(Map<EffectType<?>, ?> componentTypes) {
        if (componentTypes.isEmpty())
            return DataResult.success(DataComponentMap.EMPTY);
        return DataResult.success(DataComponentMapBuilderAccessor.effect_api$invokeBuildFromMapTrusted((Map) componentTypes));
    }

    public static DataResult<Map<DataComponentType<?>, Object>> decodeComponents(DataComponentMap map) {
        int size = map.size();
        if (size == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
        } else {
            Reference2ObjectMap<DataComponentType<?>, Object> map2 = new Reference2ObjectArrayMap<>(size);

            for (TypedDataComponent<?> component : map) {
                if (!component.type().isTransient()) {
                    map2.put(component.type(), component.value());
                }
            }
            return DataResult.success(map2);
        }
    }
}
