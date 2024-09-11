package house.greenhouse.effectapi.entity.api.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import house.greenhouse.effectapi.api.EffectAPIResourceTypes;
import house.greenhouse.effectapi.api.effect.ResourceEffect;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class EntityResourceValueArgument implements ArgumentType<Object> {
    public static final SimpleCommandExceptionType ERROR_RESOURCE_NOT_PRESENT = new SimpleCommandExceptionType(Component.translatable("argument.effectapi.resource.not_present"));
    private final String keyParam;
    private final int whitespaceBeforeKey;

    protected EntityResourceValueArgument(String keyParam, int whitespaceBeforeKey) {
        this.keyParam = keyParam;
        this.whitespaceBeforeKey = whitespaceBeforeKey;
    }

    public static EntityResourceValueArgument value(String keyParam, String commandUntilKey) {
        return new EntityResourceValueArgument(keyParam, commandUntilKey.split(" ").length);
    }

    public static EntityResourceValueArgument value(String keyParam, int whitespaceBeforeKey) {
        return new EntityResourceValueArgument(keyParam, whitespaceBeforeKey);
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
        ResourceEffect<Object> resource = InternalResourceUtil.getEffectFromId(ResourceLocation.parse(typeId));
        if (resource == null)
            throw ERROR_RESOURCE_NOT_PRESENT.createWithContext(reader);;
        reader.setCursor(valueStart);
        return EffectAPIResourceTypes.getArgumentType(resource.getResourceTypeCodec()).parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        EntityResourceEffect<Object> resource = context.getArgument(keyParam, EntityResourceEffect.class);
        return EffectAPIResourceTypes.getArgumentType(resource.getResourceTypeCodec()).listSuggestions(context, builder);
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
                return new EntityResourceValueArgument(this.keyParam, this.whitespaceBeforeKey);
            }

            @Override
            public ArgumentTypeInfo<EntityResourceValueArgument, ?> type() {
                return Info.this;
            }
        }
    }
}
