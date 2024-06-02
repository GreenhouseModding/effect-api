package dev.greenhouseteam.test.platform;

import dev.greenhouseteam.test.EffectAPITestFabric;
import dev.greenhouseteam.test.attachment.PowersAttachment;
import net.minecraft.world.entity.Entity;

public class EffectAPITestHelperFabric implements EffectAPITestHelper {
    @Override
    public PowersAttachment getPowers(Entity entity) {
        PowersAttachment attachment = entity.getAttachedOrCreate(EffectAPITestFabric.POWERS);
        attachment.init(entity);
        return attachment;
    }

    @Override
    public void removePowerAttachment(Entity entity) {
        entity.removeAttached(EffectAPITestFabric.POWERS);
    }
}