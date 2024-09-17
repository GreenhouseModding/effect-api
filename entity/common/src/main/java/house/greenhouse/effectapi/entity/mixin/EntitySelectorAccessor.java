package house.greenhouse.effectapi.entity.mixin;

import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

@Mixin(EntitySelector.class)
public interface EntitySelectorAccessor {
    @Accessor("playerName")
    String effect_api$getPlayerName();

    @Accessor("entityUUID")
    UUID effect_api$getEntityUUID();

    @Accessor("currentEntity")
    boolean effect_api$getCurrentEntity();

    @Accessor("type")
    EntityTypeTest<Entity, ?> effect_api$getType();

    @Accessor("aabb")
    AABB effect_api$getAABB();

    @Invoker("getResultLimit")
    int effect_api$invokeGetResultLimit();
}
