package house.greenhouse.effectapi.api.attachment;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface EffectsAttachment<T> {
    boolean isEmpty();
    <E extends EffectAPIEffect> List<E> getEffects(DataComponentType<List<E>> type, boolean includeInactive);

    default <E extends EffectAPIEffect> boolean hasEffectType(DataComponentType<List<E>> type) {
        return hasEffectType(type, false);
    }
    <E extends EffectAPIEffect> boolean hasEffectType(DataComponentType<List<E>> type, boolean includeInactive);

    <E extends EffectAPIEffect> boolean isActive(E effect);

    default <E extends EffectAPIEffect> boolean hasEffect(E effect) {
        return hasEffect(effect, false);
    }
    <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive);

    void addEffect(EffectAPIEffect effect, ResourceLocation source);
    void removeEffect(EffectAPIEffect effect, ResourceLocation source);

    void sync(ServerPlayer player);
    void sync();

    void tick();
    void refresh();
}
