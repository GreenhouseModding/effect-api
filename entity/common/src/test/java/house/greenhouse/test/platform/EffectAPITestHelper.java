package house.greenhouse.test.platform;

import house.greenhouse.test.attachment.DataEffectsAttachment;
import net.minecraft.world.entity.Entity;

public interface EffectAPITestHelper {
    boolean hasDataEffects(Entity entity);
    DataEffectsAttachment getDataEffects(Entity entity);
    void removeDataEffectAttachment(Entity entity);
}