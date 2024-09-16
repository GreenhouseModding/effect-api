package house.greenhouse.effectapi.entity.api.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import house.greenhouse.effectapi.api.EffectAPIDataTypes;
import house.greenhouse.effectapi.api.registry.EffectAPIRegistryKeys;
import house.greenhouse.effectapi.api.resource.Resource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EntityResourceValueArgument implements ArgumentType<Object> {
    public static final SimpleCommandExceptionType ERROR_RESOURCE_NOT_PRESENT = new SimpleCommandExceptionType(Component.translatable("argument.effectapi.resource.not_present"));
    private final String keyParam;
    private final int whitespaceBeforeKey;
    private final HolderLookup<Resource<?>> registryLookup;

    protected EntityResourceValueArgument(CommandBuildContext context, String keyParam, int whitespaceBeforeKey) {
        this.keyParam = keyParam;
        this.whitespaceBeforeKey = whitespaceBeforeKey;
        this.registryLookup = context.lookupOrThrow(EffectAPIRegistryKeys.RESOURCE);
    }

    public static EntityResourceValueArgument value(CommandBuildContext context, String keyParam, String commandUntilKey) {
        return new EntityResourceValueArgument(context, keyParam, commandUntilKey.split(" ").length);
    }

    public static EntityResourceValueArgument value(CommandBuildContext context, String keyParam, int whitespaceBeforeKey) {
        return new EntityResourceValueArgument(context, keyParam, whitespaceBeforeKey);
    }

    public static <T> T getResourceValue(CommandContext<CommandSourceStack> context, String param) {
        return (T)context.getArgument(param, Object.class);
    }

    @Override
    public Object parse(StringReader reader) throws CommandSyntaxException {
        int valueStart = reader.getCursor();
        String string = reader.getString();

        int currentWhitespaceIndex = 0;
        int index = 0;
        while (currentWhitespaceIndex < whitespaceBeforeKey && index < string.length()) {
            if (string.charAt(index) == ' ')
                ++currentWhitespaceIndex;
            ++index;
        }
        reader.setCursor(index);
        String typeId = reader.getRemaining().split(" ", 2)[0];
        Optional<Holder.Reference<Resource<?>>> resource = registryLookup.get(ResourceKey.create(EffectAPIRegistryKeys.RESOURCE, ResourceLocation.parse(typeId)));
        if (resource.isEmpty())
            throw ERROR_RESOURCE_NOT_PRESENT.createWithContext(reader);;
        reader.setCursor(valueStart);
        return EffectAPIDataTypes.getArgumentType(resource.get().value().dataType()).parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Holder<Resource<Object>> resource = context.getArgument(keyParam, Holder.class);
        return EffectAPIDataTypes.getArgumentType(resource.value().dataType()).listSuggestions(context, builder);
    }

    public static class Info implements ArgumentTypeInfo<EntityResourceValueArgument, EntityResourceValueArgument.Info.Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeUtf(template.keyParam);
            friendlyByteBuf.writeInt(template.whitespaceBeforeKey);
        }

        public EntityResourceValueArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf buf) {
            return new EntityResourceValueArgument.Info.Template(buf.readUtf(), buf.readInt());
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("key_param", template.keyParam);
            jsonObject.addProperty("whitespace_before_key", template.whitespaceBeforeKey);
        }

        public EntityResourceValueArgument.Info.Template unpack(EntityResourceValueArgument argument) {
            return new EntityResourceValueArgument.Info.Template(argument.keyParam, argument.whitespaceBeforeKey);
        }

        public final class Template implements ArgumentTypeInfo.Template<EntityResourceValueArgument> {
            final String keyParam;
            final int whitespaceBeforeKey;

            Template(String keyParam, int whitespaceBeforeKey) {
                this.keyParam = keyParam;
                this.whitespaceBeforeKey = whitespaceBeforeKey;
            }

            public EntityResourceValueArgument instantiate(CommandBuildContext context) {
                return new EntityResourceValueArgument(context, this.keyParam, this.whitespaceBeforeKey);
            }

            @Override
            public ArgumentTypeInfo<EntityResourceValueArgument, ?> type() {
                return Info.this;
            }
        }
    }
}
