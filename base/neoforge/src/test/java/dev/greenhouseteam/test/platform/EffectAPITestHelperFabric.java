package dev.greenhouseteam.test.platform;

import dev.greenhouseteam.test.EffectAPITestNeoForge;
import dev.greenhouseteam.test.attachment.PowersAttachment;
import net.minecraft.world.entity.Entity;

public class EffectAPITestHelperFabric implements EffectAPITestHelper {
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