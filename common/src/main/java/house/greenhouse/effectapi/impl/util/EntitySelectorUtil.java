package house.greenhouse.effectapi.impl.util;

import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.mixin.EntitySelectorAccessor;
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
        if (((EntitySelectorAccessor)selector).effect_api$getCurrentEntity())
            return List.of(Minecraft.getInstance().player);

        List<Entity> entities = new ArrayList<>();
        int resultLimit = ((EntitySelectorAccessor) selector).effect_api$invokeGetResultLimit();
        if (((EntitySelectorAccessor)selector).effect_api$getAABB() != null) {
            for (ServerLevel level : EffectAPI.getHelper().getServer().getAllLevels()) {
                level.getEntities(((EntitySelectorAccessor) selector).effect_api$getType(), ((EntitySelectorAccessor) selector).effect_api$getAABB().move(Minecraft.getInstance().cameraEntity.position()), entity -> entity.getType().isEnabled(Minecraft.getInstance().level.enabledFeatures()), entities, resultLimit);
            }
        }
        else
            for (ServerLevel level : EffectAPI.getHelper().getServer().getAllLevels()) {
                level.getEntities(((EntitySelectorAccessor) selector).effect_api$getType(), entity -> entity.getType().isEnabled(Minecraft.getInstance().level.enabledFeatures()), entities, resultLimit);
            }
        return entities;
    }
}
