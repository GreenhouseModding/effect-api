package house.greenhouse.test.platform;

import house.greenhouse.test.EffectAPITestNeoForge;
import house.greenhouse.test.attachment.PowersAttachment;
import net.minecraft.world.entity.Entity;

public class EffectAPITestHelperNeoForge implements EffectAPITestHelper {
    @Override
    public boolean hasPowers(Entity entity) {
        return entity.hasData(EffectAPITestNeoForge.POWERS);
    }

    @Override
    public PowersAttachment getPowers(Entity entity) {
        PowersAttachment attachment = entity.getData(EffectAPITestNeoForge.POWERS);
        attachment.init(entity);
        return attachment;
    }

    @Override
    public void removePowerAttachment(Entity entity) {
        entity.removeData(EffectAPITestNeoForge.POWERS);
    }
}