package house.greenhouse.effectapi.entity.api.effect;

import house.greenhouse.effectapi.api.effect.EffectType;
import house.greenhouse.effectapi.entity.api.EffectAPIEntityEffectTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class EntityEffectTypeBuilder<E> extends EffectType.Builder<E, Entity> {
    protected EntityEffectTypeBuilder() {
        contextCreator = EffectAPIEntityEffectTypes::buildContext;
        requiredParams = List.of(LootContextParams.THIS_ENTITY);
    }

    public static <E> EntityEffectTypeBuilder<E> builder() {
        return new EntityEffectTypeBuilder<>();
    }
}
