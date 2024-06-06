package dev.greenhouseteam.effectapi.impl.entity.client;

import dev.greenhouseteam.effectapi.mixin.EntitySelectorAccessor;
import dev.greenhouseteam.effectapi.mixin.client.ClientLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// TODO: This will crash on the server. Make server logic for this.
public class ClientEntitySelectorUtil {
    public static List<? extends Entity> findEntities(EntitySelector selector, @Nullable String playerName, @Nullable UUID uuid) {
        if (playerName != null)
            return Minecraft.getInstance().level.players().stream().filter(p -> p.getGameProfile().getName().equals(playerName)).findFirst().map(p -> List.of(p)).orElse(List.of());
        if (uuid != null)
            return Optional.ofNullable(((ClientLevelAccessor)Minecraft.getInstance().level).effectapi$invokeGetEntities().get(uuid)).map(e -> List.of(e)).orElse(List.of());
        if (((EntitySelectorAccessor)selector).effectapi$getCurrentEntity())
            return List.of(Minecraft.getInstance().player);

        List<Entity> entities = new ArrayList<>();
        int resultLimit = ((EntitySelectorAccessor) selector).effectapi$invokeGetResultLimit();
        if (((EntitySelectorAccessor)selector).effectapi$getAABB() != null)
            Minecraft.getInstance().level.getEntities(((EntitySelectorAccessor) selector).effectapi$getType(), ((EntitySelectorAccessor) selector).effectapi$getAABB().move(Minecraft.getInstance().cameraEntity.position()), entity -> entity.getType().isEnabled(Minecraft.getInstance().level.enabledFeatures()), entities, resultLimit);
        else
            ((ClientLevelAccessor)Minecraft.getInstance().level).effectapi$invokeGetEntities().get(((EntitySelectorAccessor) selector).effectapi$getType(), obj -> {
                if (obj.getType().isEnabled(Minecraft.getInstance().level.enabledFeatures())) {
                    entities.add(obj);
                    if (entities.size() >= resultLimit) {
                        return AbortableIterationConsumer.Continuation.ABORT;
                    }
                }

                return AbortableIterationConsumer.Continuation.CONTINUE;
            });
        return entities;
    }
}
