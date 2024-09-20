package house.greenhouse.effectapi.api.attachment;

import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.effect.EffectHolder;
import house.greenhouse.effectapi.api.effect.EffectType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface EffectsAttachment {
    boolean isEmpty();
    <E extends EffectAPIEffect> E getEffect(EffectHolder<E> holder);

    <E extends EffectAPIEffect> List<E> getEffects(EffectType<E> type, boolean includeInactive);

    default <E extends EffectAPIEffect> boolean hasEffectType(EffectType<E> type) {
        return hasEffectType(type, false);
    }
    <E extends EffectAPIEffect> boolean hasEffectType(EffectType<E> type, boolean includeInactive);

    default <E extends EffectAPIEffect> boolean hasEffect(EffectHolder<E> effect) {
        return hasEffect(effect, false);
    }
    <E extends EffectAPIEffect> boolean hasEffect(EffectHolder<E> holder, boolean includeInactive);

    default <E extends EffectAPIEffect> boolean hasEffect(E effect) {
        return hasEffect(effect, false);
    }
    <E extends EffectAPIEffect> boolean hasEffect(E effect, boolean includeInactive);

    void addEffect(EffectHolder<EffectAPIEffect> effect, ResourceLocation source);
    void removeEffect(EffectHolder<EffectAPIEffect> effect, ResourceLocation source);

    void sync(ServerPlayer player);
    void sync();

    void tick();
    void refresh();
}
