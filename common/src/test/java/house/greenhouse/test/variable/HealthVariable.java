package house.greenhouse.test.variable;

import com.mojang.serialization.MapCodec;
import house.greenhouse.effectapi.impl.registry.EffectAPIDataTypes;
import house.greenhouse.effectapi.api.variable.DataType;
import house.greenhouse.effectapi.api.variable.Variable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Collection;
import java.util.List;

public class HealthVariable implements Variable<Float> {
    public static final MapCodec<HealthVariable> CODEC = MapCodec.unit(HealthVariable::new);

    @Override
    public Float get(LootContext context) {
        Entity entity = context.getParam(LootContextParams.THIS_ENTITY);
        if (entity instanceof LivingEntity living)
            return living.getHealth();
        return 0.0F;
    }

    @Override
    public DataType<Float> dataType() {
        return EffectAPIDataTypes.FLOAT;
    }

    @Override
    public Collection<LootContextParam<?>> requiredParams() {
        return List.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public MapCodec<? extends Variable<?>> codec() {
        return CODEC;
    }
}
