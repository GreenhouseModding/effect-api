package house.greenhouse.test.predicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record OnFirePredicate(LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final MapCodec<OnFirePredicate> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(OnFirePredicate::entityTarget)
    ).apply(inst, OnFirePredicate::new));
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

    @Override
    public boolean test(LootContext context) {
        if (!context.hasParam(entityTarget.getParam()))
            return false;
        return context.getParam(entityTarget.getParam()).isOnFire();
    }

    @Override
    public LootItemConditionType getType() {
        return TYPE;
    }
}
