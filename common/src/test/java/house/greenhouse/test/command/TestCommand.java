package house.greenhouse.test.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import house.greenhouse.effectapi.api.EntityResourceAPI;
import house.greenhouse.effectapi.api.command.EntityResourceArgument;
import house.greenhouse.effectapi.api.command.EntityResourceValueArgument;
import house.greenhouse.effectapi.api.resource.Resource;
import house.greenhouse.effectapi.impl.EffectAPI;
import house.greenhouse.test.DataEffect;
import house.greenhouse.test.EffectAPITest;
import house.greenhouse.test.attachment.DataEffectsAttachment;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class TestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {

        LiteralCommandNode<CommandSourceStack> testNode = Commands
                .literal("effectapi")
                .build();

        LiteralCommandNode<CommandSourceStack> powerNode = Commands
                .literal("effect")
                .build();

        LiteralCommandNode<CommandSourceStack> resourceNode = Commands
                .literal("resource")
                .build();

        LiteralCommandNode<CommandSourceStack> addPowerNode = Commands
                .literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("effect", ResourceArgument.resource(context, EffectAPITest.DATA_EFFECT))
                                .executes(TestCommand::addEffect)))
                .build();
        LiteralCommandNode<CommandSourceStack> removePowerNode = Commands
                .literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("effect", ResourceArgument.resource(context, EffectAPITest.DATA_EFFECT))
                                .executes(TestCommand::removeEffect)))
                .build();
        LiteralCommandNode<CommandSourceStack> listPowersNode = Commands
                .literal("list")
                .then(Commands.argument("target", EntityArgument.entity())
                                .executes(TestCommand::listEffects))
                .build();
        LiteralCommandNode<CommandSourceStack> clearPowersNode = Commands
                .literal("clear")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(TestCommand::clearEffects))
                .build();

        LiteralCommandNode<CommandSourceStack> setResourceNode = Commands
                .literal("set")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("key", EntityResourceArgument.resource(context, "targets", "effectapi resource set", EffectAPITest.MOD_ID))
                                .then(Commands.argument("value", EntityResourceValueArgument.value(context, "key","effectapi resource set <targets>"))
                                    .executes(TestCommand::setResource))))
                .build();
        LiteralCommandNode<CommandSourceStack> getResourceNode = Commands
                .literal("get")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("key", EntityResourceArgument.resource(context, "target", "effectapi resource get", EffectAPITest.MOD_ID))
                                .executes(TestCommand::getResource)))
                .build();

        powerNode.addChild(addPowerNode);
        powerNode.addChild(removePowerNode);
        powerNode.addChild(listPowersNode);
        powerNode.addChild(clearPowersNode);

        resourceNode.addChild(getResourceNode);
        resourceNode.addChild(setResourceNode);

        testNode.addChild(powerNode);
        testNode.addChild(resourceNode);

        dispatcher.getRoot().addChild(testNode);
    }

    private static int addEffect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
        Holder<DataEffect> effect = ResourceArgument.getResource(context, "effect", EffectAPITest.DATA_EFFECT);

        int successes = 0;
        Entity firstEntity = null;

        for (Entity entity : entities) {
            if (firstEntity == null)
                firstEntity = entity;
            DataEffectsAttachment attachment = EffectAPITest.getHelper().getDataEffects(entity);
            if (!attachment.hasEffect(effect)) {
                attachment.addEffect(effect);
                attachment.sync();
                ++successes;
            }
        }

        int finalSuccesses = successes;
        if (entities.size() > 1) {
            if (successes > 0)
                context.getSource().sendSuccess(() -> Component.literal("Added power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\" to " + finalSuccesses + " entities."), true);
            else
                context.getSource().sendFailure(Component.literal("All of the specified entities already have the power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\"."));
        } else {
            Entity finalFirstEntity = firstEntity;
            if (successes > 0)
                context.getSource().sendSuccess(() -> Component.literal("Added power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\" to " + finalFirstEntity.getScoreboardName() + "."), true);
            else
                context.getSource().sendFailure(Component.literal(finalFirstEntity.getScoreboardName() + " already has the power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\"."));
        }
        return successes;
    }

    private static int removeEffect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
        Holder<DataEffect> effect = ResourceArgument.getResource(context, "effect", EffectAPITest.DATA_EFFECT);

        int successes = 0;
        Entity firstEntity = null;

        for (Entity entity : entities) {
            if (firstEntity == null)
                firstEntity = entity;
            if (!EffectAPITest.getHelper().hasDataEffects(entity))
                continue;
            DataEffectsAttachment attachment = EffectAPITest.getHelper().getDataEffects(entity);
            if (attachment.hasEffect(effect)) {
                attachment.removeEffect(effect);
                attachment.sync();
                if (attachment.isEmpty())
                    EffectAPITest.getHelper().removeDataEffectAttachment(entity);
                ++successes;
            }
        }

        int finalSuccesses = successes;
        if (entities.size() > 1) {
            if (successes > 0)
                context.getSource().sendSuccess(() -> Component.literal("Removed power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\" from " + finalSuccesses + " entities."), true);
            else
                context.getSource().sendFailure(Component.literal("All of the specified entities do not have the power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\"."));
        } else {
            Entity finalFirstEntity = firstEntity;
            if (successes > 0)
                context.getSource().sendSuccess(() -> Component.literal("Removed power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\" from " + finalFirstEntity.getScoreboardName() + "."), true);
            else
                context.getSource().sendFailure(Component.literal(finalFirstEntity.getScoreboardName() + " does not have the power \"" + effect.unwrapKey().map(ResourceKey::location).orElse(null) + "\"."));
        }
        return successes;
    }

    private static int listEffects(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        if (!EffectAPITest.getHelper().hasDataEffects(entity)) {
            context.getSource().sendFailure(Component.literal("Entity does not have any powers."));
            return 0;
        }

        DataEffectsAttachment attachment = EffectAPITest.getHelper().getDataEffects(entity);
        if (attachment == null) {
            context.getSource().sendFailure(Component.literal("Entity does not have any powers."));
            return 0;
        }

        StringBuilder stringBuilder = new StringBuilder("Entity has powers: [");
        for (int i = 0; i < attachment.getPowers().size(); ++i) {
            if (i != 0)
                stringBuilder.append(", ");
            stringBuilder.append(attachment.getPowers().get(i).unwrapKey().map(ResourceKey::location).orElse(null));
        }
        stringBuilder.append("].");

        context.getSource().sendSuccess(() -> Component.literal(stringBuilder.toString()), true);
        return 1;
    }


    private static int clearEffects(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");

        int successes = 0;
        Entity firstEntity = null;

        for (Entity entity : entities) {
            if (firstEntity == null)
                firstEntity = entity;
            if (EffectAPITest.getHelper().hasDataEffects(entity)) {
                EffectAPITest.getHelper().removeDataEffectAttachment(entity);
                ++successes;
            }
        }

        int finalSuccesses = successes;
        if (entities.size() > 1) {
            if (successes > 0)
                context.getSource().sendSuccess(() -> Component.literal("Removed all powers from " + finalSuccesses + " entities."), true);
            else
                context.getSource().sendFailure(Component.literal("All of the specified entities do not have any powers."));
        } else {
            Entity finalFirstEntity = firstEntity;
            if (successes > 0)
                context.getSource().sendSuccess(() -> Component.literal("Removed all powers from " + finalFirstEntity.getScoreboardName() + "."), true);
            else
                context.getSource().sendFailure(Component.literal(finalFirstEntity.getScoreboardName() + " does not have any powers."));
        }
        return successes;
    }

    private static int setResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");

        Holder<Resource<Object>> resource = EntityResourceArgument.getResource(context, "key");
        Object value = EntityResourceValueArgument.getResourceValue(context, "value");

        int successes = 0;

        for (Entity entity : entities) {
            if (EffectAPI.getHelper().getResources(entity) != null && EffectAPI.getHelper().getResources(entity).hasResource(resource)) {
                EntityResourceAPI.setResourceValue(entity, resource, value);
                ++successes;
            }
        }

        if (successes == 0)
            context.getSource().sendFailure(Component.literal("None of the specified entities have the resource \"" + resource.unwrapKey().get().location() + "\"."));
        else if (successes == 1)
            context.getSource().sendSuccess(() -> Component.literal("Set resource \"" + resource.unwrapKey().get().location() + "\" to " + value + " on entity."), true);
        else if (successes > 1) {
            int finalSuccesses = successes;
            context.getSource().sendSuccess(() -> Component.literal("Set resource \"" + resource.unwrapKey().get().location() + "\" to " + value + " for " + finalSuccesses + " entities."), true);
        }

        return successes;
    }

    private static int getResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Resource<Object>> resource = EntityResourceArgument.getResource(context, "key");

        if (EffectAPI.getHelper().getResources(entity) == null || !EffectAPI.getHelper().getResources(entity).hasResource(resource) || !EffectAPI.getHelper().getResources(entity).hasSourceFromMod(resource, EffectAPITest.MOD_ID)) {
            context.getSource().sendFailure(Component.literal("Entity does not have resource \"" + resource + "\"."));
            return 0;
        }

        Object value = EntityResourceAPI.getResourceValue(entity, resource);
        String stringValue = value instanceof String str ? "\"" + str + "\"" : value.toString();

        context.getSource().sendSuccess(() -> Component.literal("Resource \"" + resource.unwrapKey().get().location() + "\" is " + stringValue + "."), true);
        if (value instanceof Number number)
            return number.intValue();
        if (value instanceof Boolean bool)
            return bool.booleanValue() ? 1 : 0;

        return 1;
    }
}