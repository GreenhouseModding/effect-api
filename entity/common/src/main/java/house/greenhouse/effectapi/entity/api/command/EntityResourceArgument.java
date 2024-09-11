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
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.client.util.ClientEntitySelectorUtil;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.entity.impl.util.EntitySelectorUtil;
import house.greenhouse.effectapi.entity.mixin.EntitySelectorAccessor;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EntityResourceArgument implements ArgumentType<ResourceEffect<?>> {
    public static final SimpleCommandExceptionType ERROR_RESOURCE_NOT_PRESENT = new SimpleCommandExceptionType(Component.translatable("argument.effectapi.resource.not_present"));

    private final String entitiesParam;
    private final int whitespaceBeforeEntities;
    private final ResourceLocation source;

    protected EntityResourceArgument(String entitiesParam, int whitespaceBeforeEntities, ResourceLocation source) {
        this.entitiesParam = entitiesParam;
        this.whitespaceBeforeEntities = whitespaceBeforeEntities;
        this.source = source;
    }

    public static EntityResourceArgument resource(String entitiesParam, String commandUntilEntities, ResourceLocation source) {
        return new EntityResourceArgument(entitiesParam, commandUntilEntities.split(" ").length, source);
    }

    public static <T> EntityResourceEffect<T> getResource(CommandContext<CommandSourceStack> context, String param) {
        return context.getArgument(param, EntityResourceEffect.class);
    }

    @Override
    public ResourceEffect<?> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation typeId = ResourceLocation.read(reader);
        ResourceEffect<?> resource = InternalResourceUtil.getEffectFromId(typeId);

        if (resource == null)
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
        List<? extends Entity> entities = EntitySelectorUtil.findEntitiesServer(selector, ((EntitySelectorAccessor)selector).effectapi$getPlayerName(), ((EntitySelectorAccessor)selector).effectapi$getEntityUUID());

        if (!getResourceEffectsFromSource(entities).contains(typeId))
            throw ERROR_RESOURCE_NOT_PRESENT.createWithContext(reader);

        return resource;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        EntitySelector selector = context.getArgument(entitiesParam, EntitySelector.class);

        List<? extends Entity> entities = ClientEntitySelectorUtil.findEntitiesClient(selector, ((EntitySelectorAccessor)selector).effectapi$getPlayerName(), ((EntitySelectorAccessor)selector).effectapi$getEntityUUID());

        for (ResourceLocation val : getResourceEffectsFromSource(entities))
            builder.suggest(val.toString());

        return CompletableFuture.completedFuture(builder.build());
    }

    private List<ResourceLocation> getResourceEffectsFromSource(List<? extends Entity> entities) {
        return InternalResourceUtil.getIdMap().keySet().stream().filter(id -> entities.stream().anyMatch(entity -> {
                    ResourcesAttachment attachment = EffectAPIEntity.getHelper().getResources(entity);
                    if (attachment == null)
                        return false;
                    ResourceEffect.ResourceHolder<?> value = attachment.getResourceHolder(id);
                    return value != null && value.getSource() != null && value.getSource().equals(source);
                })).toList();
    }

    public static class Info implements ArgumentTypeInfo<EntityResourceArgument, EntityResourceArgument.Info.Template> {
        @Override
        public void serializeToNetwork(EntityResourceArgument.Info.Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(template.entitiesParam);
            friendlyByteBuf.writeInt(template.whitespaceBeforeEntities);
            friendlyByteBuf.writeResourceLocation(template.source);
        }

        public EntityResourceArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf buf) {
            return new EntityResourceArgument.Info.Template(buf.readUtf(), buf.readInt(), buf.readResourceLocation());
        }

        @Override
        public void serializeToJson(EntityResourceArgument.Info.Template template, JsonObject jsonObject) {
            jsonObject.addProperty("entities_param", template.entitiesParam);
            jsonObject.addProperty("whitespace_before_entities", template.whitespaceBeforeEntities);
            jsonObject.addProperty("source", template.source.toString());
        }

        public EntityResourceArgument.Info.Template unpack(EntityResourceArgument argument) {
            return new EntityResourceArgument.Info.Template(argument.entitiesParam, argument.whitespaceBeforeEntities, argument.source);
        }

        public final class Template implements ArgumentTypeInfo.Template<EntityResourceArgument> {
            private final String entitiesParam;
            private final int whitespaceBeforeEntities;
            private final ResourceLocation source;

            Template(String entitiesParam, int whitespaceBeforeEntities, ResourceLocation source) {
                this.entitiesParam = entitiesParam;
                this.whitespaceBeforeEntities = whitespaceBeforeEntities;
                this.source = source;
            }

            public EntityResourceArgument instantiate(CommandBuildContext context) {
                return new EntityResourceArgument(entitiesParam, whitespaceBeforeEntities, source);
            }

            @Override
            public ArgumentTypeInfo<EntityResourceArgument, ?> type() {
                return EntityResourceArgument.Info.this;
            }
        }
    }
}
