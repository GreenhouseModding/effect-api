package house.greenhouse.test.platform;

import house.greenhouse.test.attachment.PowersAttachment;
import net.minecraft.world.entity.Entity;

public interface EffectAPITestHelper {
    boolean hasPowers(Entity entity);
    PowersAttachment getPowers(Entity entity);
    void removePowerAttachment(Entity entity);
}