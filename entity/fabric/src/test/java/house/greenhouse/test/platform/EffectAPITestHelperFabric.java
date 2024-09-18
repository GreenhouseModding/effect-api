package house.greenhouse.test.platform;

import house.greenhouse.test.EffectAPITestFabric;
import house.greenhouse.test.attachment.DataEffectsAttachment;
import net.minecraft.world.entity.Entity;

public class EffectAPITestHelperFabric implements EffectAPITestHelper {
    @Override
    public boolean hasDataEffects(Entity entity) {
        return entity.hasAttached(EffectAPITestFabric.DATA_EFFECTS);
    }

    @Override
    public DataEffectsAttachment getDataEffects(Entity entity) {
        DataEffectsAttachment attachment = entity.getAttachedOrCreate(EffectAPITestFabric.DATA_EFFECTS);
        attachment.init(entity);
        return attachment;
    }

    @Override
    public void removeDataEffectAttachment(Entity entity) {
        entity.removeAttached(EffectAPITestFabric.DATA_EFFECTS);
    }
}