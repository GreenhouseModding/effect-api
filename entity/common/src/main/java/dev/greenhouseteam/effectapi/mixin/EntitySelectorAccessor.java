package dev.greenhouseteam.effectapi.mixin;

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
    String effectapi$getPlayerName();

    @Accessor("entityUUID")
    UUID effectapi$getEntityUUID();

    @Accessor("currentEntity")
    boolean effectapi$getCurrentEntity();

    @Accessor("type")
    EntityTypeTest<Entity, ?> effectapi$getType();

    @Accessor("aabb")
    AABB effectapi$getAABB();

    @Invoker("getResultLimit")
    int effectapi$invokeGetResultLimit();
}
