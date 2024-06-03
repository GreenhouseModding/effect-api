package dev.greenhouseteam.effectapi.api.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DataResourceArgument implements ArgumentType<ResourceEffect<?>> {
    public static final SimpleCommandExceptionType ERROR_RESOURCE_NOT_PRESENT = new SimpleCommandExceptionType(Component.translatable("argument.effectapi.resource.not_present"));

    protected DataResourceArgument() {

    }

    public static DataResourceArgument resource() {
        return new DataResourceArgument();
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
        for (ResourceLocation val : ResourceEffect.getIdMap().entrySet().stream()
                .filter(entry -> !entry.getValue().isHidden()).map(Map.Entry::getKey).toList())
            builder.suggest(val.toString());

        return CompletableFuture.completedFuture(builder.build());
    }
}
