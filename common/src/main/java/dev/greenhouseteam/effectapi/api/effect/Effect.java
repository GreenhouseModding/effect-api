package dev.greenhouseteam.effectapi.api.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.greenhouseteam.effectapi.api.registry.EffectAPIRegistries;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Map;

public abstract class Effect {
    public static final Codec<DataComponentMap> CODEC = Codec.dispatchedMap(EffectAPIRegistries.EFFECT.byNameCodec(), DataComponentType::codecOrThrow).flatComapMap(map -> {
        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (Map.Entry<DataComponentType<?>, ?> entry : map.entrySet()) {
            builder.set((DataComponentType<Object>) entry.getKey(), entry.getValue());
        }
        return builder.build();
    }, componentMap -> {
        int i = componentMap.size();
        if (i == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
        } else {
            Reference2ObjectMap<DataComponentType<?>, Object> reference2objectmap = new Reference2ObjectArrayMap<>(i);

            for (TypedDataComponent<?> typeddatacomponent : componentMap) {
                if (!typeddatacomponent.type().isTransient()) {
                    reference2objectmap.put(typeddatacomponent.type(), typeddatacomponent.value());
                }
            }

            return (DataResult) DataResult.success(reference2objectmap);
        }
    });

    private Effect parent;

    public abstract Codec<? extends Effect> codec();

    public abstract DataComponentType<?> type();

    public void onAdded(Entity entity) {

    }

    public void onRemoved(Entity entity) {

    }

    public void tick(Entity entity) {

    }

    public boolean isActive(Entity entity) {
        return parent == null || parent.isActive(entity);
    }

    public boolean isContainer() {
        return false;
    }

    public Effect parent() {
        return parent;
    }

    protected void parentChildren() {
        childrenAsEffects().forEach(effect -> effect.parent = this);
    }

    public DataComponentMap children() {
        return DataComponentMap.EMPTY;
    }

    public List<Effect> childrenAsEffects() {
        return children().stream().filter(component -> component.value() instanceof List).flatMap(component -> ((List<Effect>)component.value()).stream()).toList();
    }
}
