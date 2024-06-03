package dev.greenhouseteam.effectapi.api.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.greenhouseteam.effectapi.api.attachment.ResourcesAttachment;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.client.EntitySelectorUtil;
import dev.greenhouseteam.effectapi.mixin.EntitySelectorAccessor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataResourceArgument implements ArgumentType<ResourceEffect<?>> {
    public static final SimpleCommandExceptionType ERROR_RESOURCE_NOT_PRESENT = new SimpleCommandExceptionType(Component.translatable("argument.effectapi.resource.not_present"));

    String entitiesParam;

    protected DataResourceArgument(String entitiesParam) {
        this.entitiesParam = entitiesParam;
    }

    public static DataResourceArgument resource(String entitiesParam) {
        return new DataResourceArgument(entitiesParam);
    }

    public static <T> ResourceEffect<T> getResource(CommandContext<CommandSourceStack> context, String param) {
        return context.getArgument(param, ResourceEffect.class);
    }

    @Override
    public ResourceEffect<?> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation typeId = ResourceLocation.read(reader);
        ResourceEffect<?> resource = ResourceEffect.getEffectFromId(typeId);
        if (resource == null)
            throw ERROR_RESOURCE_NOT_PRESENT.createWithContext(reader);
        return resource;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        EntitySelector selector = context.getArgument(entitiesParam, EntitySelector.class);

        List<? extends Entity> entities = EntitySelectorUtil.findEntities(selector, ((EntitySelectorAccessor)selector).effectapi$getPlayerName(), ((EntitySelectorAccessor)selector).effectapi$getEntityUUID());

        for (ResourceLocation val : ResourceEffect.getIdMap().keySet().stream()
                .filter(resourceEffect -> entities.stream().allMatch(entity -> {
                    ResourcesAttachment attachment = EffectAPI.getHelper().getResources(entity);
                    if (attachment == null)
                        return false;
                    return attachment.resources().containsKey(resourceEffect);
                })).toList())
            builder.suggest(val.toString());

        return CompletableFuture.completedFuture(builder.build());
    }

    public static class Info implements ArgumentTypeInfo<DataResourceArgument, DataResourceArgument.Info.Template> {
        @Override
        public void serializeToNetwork(DataResourceArgument.Info.Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(template.entitiesParam);
        }

        public DataResourceArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf buf) {
            return new DataResourceArgument.Info.Template(buf.readUtf());
        }

        @Override
        public void serializeToJson(DataResourceArgument.Info.Template template, JsonObject jsonObject) {
            jsonObject.addProperty("entities_param", template.entitiesParam);
        }

        public DataResourceArgument.Info.Template unpack(DataResourceArgument argument) {
            return new DataResourceArgument.Info.Template(argument.entitiesParam);
        }

        public final class Template implements ArgumentTypeInfo.Template<DataResourceArgument> {
            String entitiesParam;

            Template(String entitiesParam) {
                this.entitiesParam = entitiesParam;
            }

            public DataResourceArgument instantiate(CommandBuildContext context) {
                return new DataResourceArgument(entitiesParam);
            }

            @Override
            public ArgumentTypeInfo<DataResourceArgument, ?> type() {
                return DataResourceArgument.Info.this;
            }
        }
    }
}
