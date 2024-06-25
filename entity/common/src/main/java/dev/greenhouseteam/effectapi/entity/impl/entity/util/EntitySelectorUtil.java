package dev.greenhouseteam.effectapi.entity.impl.entity.util;

import dev.greenhouseteam.effectapi.entity.mixin.EntitySelectorAccessor;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntitySelectorUtil {
    public static List<? extends Entity> findEntitiesServer(EntitySelector selector, @Nullable String playerName, @Nullable UUID uuid) {
        if (playerName != null)
            return List.of(EffectAPI.getHelper().getServer().getPlayerList().getPlayerByName(playerName));
        if (uuid != null)
            return List.of(EffectAPI.getHelper().getServer().getPlayerList().getPlayer(uuid));
        if (((EntitySelectorAccessor)selector).effectapi$getCurrentEntity())
            return List.of(Minecraft.getInstance().player);

        List<Entity> entities = new ArrayList<>();
        int resultLimit = ((EntitySelectorAccessor) selector).effectapi$invokeGetResultLimit();
        if (((EntitySelectorAccessor)selector).effectapi$getAABB() != null) {
            for (ServerLevel level : EffectAPI.getHelper().getServer().getAllLevels()) {
                level.getEntities(((EntitySelectorAccessor) selector).effectapi$getType(), ((EntitySelectorAccessor) selector).effectapi$getAABB().move(Minecraft.getInstance().cameraEntity.position()), entity -> entity.getType().isEnabled(Minecraft.getInstance().level.enabledFeatures()), entities, resultLimit);
            }
        }
        else
            for (ServerLevel level : EffectAPI.getHelper().getServer().getAllLevels()) {
                level.getEntities(((EntitySelectorAccessor) selector).effectapi$getType(), entity -> entity.getType().isEnabled(Minecraft.getInstance().level.enabledFeatures()), entities, resultLimit);
            }
        return entities;
    }
}
