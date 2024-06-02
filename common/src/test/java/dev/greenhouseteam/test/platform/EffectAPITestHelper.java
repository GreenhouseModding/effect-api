package dev.greenhouseteam.test.platform;

import dev.greenhouseteam.test.attachment.PowersAttachment;
import net.minecraft.world.entity.Entity;

public interface EffectAPITestHelper {
    PowersAttachment getPowers(Entity entity);
    void removePowerAttachment(Entity entity);
}