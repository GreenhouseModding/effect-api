package house.greenhouse.test.platform;

import house.greenhouse.test.EffectAPITestNeoForge;
import house.greenhouse.test.attachment.DataEffectsAttachment;
import net.minecraft.world.entity.Entity;

public class EffectAPITestHelperNeoForge implements EffectAPITestHelper {
    @Override
    public boolean hasDataEffects(Entity entity) {
        return entity.hasData(EffectAPITestNeoForge.DATA_EFFECTS);
    }

    @Override
    public DataEffectsAttachment getDataEffects(Entity entity) {
        DataEffectsAttachment attachment = entity.getData(EffectAPITestNeoForge.DATA_EFFECTS);
        attachment.init(entity);
        return attachment;
    }

    @Override
    public void removeDataEffectAttachment(Entity entity) {
        entity.removeData(EffectAPITestNeoForge.DATA_EFFECTS);
    }
}