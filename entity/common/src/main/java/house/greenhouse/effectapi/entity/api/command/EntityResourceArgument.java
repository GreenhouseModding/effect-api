package house.greenhouse.effectapi.entity.api.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import house.greenhouse.effectapi.api.attachment.ResourcesAttachment;
import house.greenhouse.effectapi.api.effect.EffectAPIEffect;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.attachment.ResourcesAttachmentImpl;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.client.util.ClientEntitySelectorUtil;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.entity.impl.util.EntitySelectorUtil;
import house.greenhouse.effectapi.entity.mixin.EntitySelectorAccessor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EntityResourceArgument implements ArgumentType<Holder<Resource<?>>> {
    public static final SimpleCommandExceptionType ERROR_RESOURCE_NOT_PRESENT = new SimpleCommandExceptionType(Component.translatable("argument.effectapi.resource.not_present"));

    private final String entitiesParam;
    private final int whitespaceBeforeEntities;
    private final String modId;
    private final HolderLookup<Resource<?>> registryLookup;

    protected EntityResourceArgument(CommandBuildContext context, String entitiesParam, int whitespaceBeforeEntities, String modId) {
        this.entitiesParam = entitiesParam;
        this.whitespaceBeforeEntities = whitespaceBeforeEntities;
        this.modId = modId;
        this.registryLookup = context.lookupOrThrow(EffectAPIRegistryKeys.RESOURCE);
    }

    public static EntityResourceArgument resource(CommandBuildContext context, String entitiesParam, String commandUntilEntities, String modId) {
        return new EntityResourceArgument(context, entitiesParam, commandUntilEntities.split(" ").length, modId);
    }

    public static <T> Holder<Resource<T>> getResource(CommandContext<CommandSourceStack> context, String param) {
        return context.getArgument(param, Holder.class);
    }

    @Override
    public Holder<Resource<?>> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation typeId = ResourceLocation.read(reader);
        Optional<Holder.Reference<Resource<?>>> resource = registryLookup.get(ResourceKey.create(EffectAPIRegistryKeys.RESOURCE, typeId));

        if (resource.isEmpty())
            throw ERROR_RESOURCE_NOT_PRESENT.createWithContext(reader);

        int cursor = reader.getCursor();
        String string = reader.getString();
        int currentWhitespaceIndex = 0;
        int index = 0;
        while (currentWhitespaceIndex < whitespaceBeforeEntities && index < string.length()) {
            if (string.charAt(index) == ' ')
                ++currentWhitespaceIndex;
            ++index;
        }
        reader.setCursor(index);
        String entitiesString = reader.getRemaining().split(" ", 2)[0];
        reader.setCursor(cursor);

        StringReader entitiesReader = new StringReader(entitiesString);
        EntitySelector selector = EntityArgument.entities().parse(entitiesReader);
        List<? extends Entity> entities = EntitySelectorUtil.findEntitiesServer(selector, ((EntitySelectorAccessor)selector).effect_api$getPlayerName(), ((EntitySelectorAccessor)selector).effect_api$getEntityUUID());

        List<Holder.Reference<Resource<?>>> resources = getResourcesFromSource(entities);
        if (resources.stream().noneMatch(reference -> reference.is(resource.get())))
            throw ERROR_RESOURCE_NOT_PRESENT.createWithContext(reader);

        return resource.get();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        EntitySelector selector = context.getArgument(entitiesParam, EntitySelector.class);

        List<? extends Entity> entities = ClientEntitySelectorUtil.findEntitiesClient(selector, ((EntitySelectorAccessor)selector).effect_api$getPlayerName(), ((EntitySelectorAccessor)selector).effect_api$getEntityUUID());

        for (Holder<Resource<?>> val : getResourcesFromSource(entities))
            builder.suggest(val.unwrapKey().get().location().toString());

        return CompletableFuture.completedFuture(builder.build());
    }

    private List<Holder.Reference<Resource<?>>> getResourcesFromSource(List<? extends Entity> entities) {
        return entities.stream().flatMap(entity -> {
            ResourcesAttachment attachment = EffectAPIEntity.getHelper().getResources(entity);
            if (attachment == null)
                return Stream.of();
            return entity.level().registryAccess().registryOrThrow(EffectAPIRegistryKeys.RESOURCE).holders().filter(resource ->
                    ((ResourcesAttachmentImpl) attachment).getSources((Holder<Resource<Object>>)(Holder) resource).stream().anyMatch(location -> location.getNamespace().equals(modId)));
        }).toList();
    }

    public static class Info implements ArgumentTypeInfo<EntityResourceArgument, EntityResourceArgument.Info.Template> {
        @Override
        public void serializeToNetwork(EntityResourceArgument.Info.Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(template.entitiesParam);
            friendlyByteBuf.writeInt(template.whitespaceBeforeEntities);
            friendlyByteBuf.writeUtf(template.modId);
        }

        public EntityResourceArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf buf) {
            return new EntityResourceArgument.Info.Template(buf.readUtf(), buf.readInt(), buf.readUtf());
        }

        @Override
        public void serializeToJson(EntityResourceArgument.Info.Template template, JsonObject jsonObject) {
            jsonObject.addProperty("entities_param", template.entitiesParam);
            jsonObject.addProperty("whitespace_before_entities", template.whitespaceBeforeEntities);
            jsonObject.addProperty("source", template.modId);
        }

        public EntityResourceArgument.Info.Template unpack(EntityResourceArgument argument) {
            return new EntityResourceArgument.Info.Template(argument.entitiesParam, argument.whitespaceBeforeEntities, argument.modId);
        }

        public final class Template implements ArgumentTypeInfo.Template<EntityResourceArgument> {
            private final String entitiesParam;
            private final int whitespaceBeforeEntities;
            private final String modId;

            Template(String entitiesParam, int whitespaceBeforeEntities, String modId) {
                this.entitiesParam = entitiesParam;
                this.whitespaceBeforeEntities = whitespaceBeforeEntities;
                this.modId = modId;
            }

            public EntityResourceArgument instantiate(CommandBuildContext context) {
                return new EntityResourceArgument(context, entitiesParam, whitespaceBeforeEntities, modId);
            }

            @Override
            public ArgumentTypeInfo<EntityResourceArgument, ?> type() {
                return EntityResourceArgument.Info.this;
            }
        }
    }
}
