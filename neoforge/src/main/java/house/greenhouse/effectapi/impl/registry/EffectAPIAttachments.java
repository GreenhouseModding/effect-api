package house.greenhouse.effectapi.impl.registry;

import house.greenhouse.effectapi.api.attachment.EffectsAttachment;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.impl.attachment.EffectsAttachmentImpl;
import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.effectapi.impl.registry.internal.RegistrationCallback;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;

public class EffectAPIAttachments {
    public static final AttachmentType<EffectsAttachment> EFFECTS = AttachmentType.builder(() -> (EffectsAttachment)new EffectsAttachmentImpl()).build();
    public static final AttachmentType<ResourcesAttachment> RESOURCES = AttachmentType
            .<ResourcesAttachment>builder(() -> new ResourcesAttachmentImpl(new HashMap<>()))
            .serialize(ResourcesAttachmentImpl.CODEC)
            .copyOnDeath()
            .build();
    public static void registerAll(RegistrationCallback<AttachmentType<?>> callback) {
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EffectAPI.asResource("effects"), EFFECTS);
        callback.register(NeoForgeRegistries.ATTACHMENT_TYPES, EffectAPI.asResource("resources"), RESOURCES);
    }
}