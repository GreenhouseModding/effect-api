package dev.greenhouseteam.test.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.greenhouseteam.effectapi.api.command.DataResourceArgument;
import dev.greenhouseteam.effectapi.api.command.DataResourceValueArgument;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.test.EffectAPITest;
import dev.greenhouseteam.test.Power;
import dev.greenhouseteam.test.attachment.PowersAttachment;
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
                .literal("power")
                .build();

        LiteralCommandNode<CommandSourceStack> resourceNode = Commands
                .literal("resource")
                .build();

        LiteralCommandNode<CommandSourceStack> grantPowerNode = Commands
                .literal("grant")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("power", ResourceArgument.resource(context, EffectAPITest.POWER))
                                .executes(TestCommand::grantPower)))
                .build();
        LiteralCommandNode<CommandSourceStack> revokePowerNode = Commands
                .literal("revoke")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("power", ResourceArgument.resource(context, EffectAPITest.POWER))
                                .executes(TestCommand::revokePower)))
                .build();

        LiteralCommandNode<CommandSourceStack> setResourceNode = Commands
                .literal("set")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("key", DataResourceArgument.resource("targets"))
                                .then(Commands.argument("value", DataResourceValueArgument.value("key","effectapi resource set <target>"))
                                    .executes(TestCommand::setResource))))
                .build();

        LiteralCommandNode<CommandSourceStack> getResourceNode = Commands
                .literal("get")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("key", DataResourceArgument.resource("target"))
                                .executes(TestCommand::getResource)))
                .build();

        powerNode.addChild(grantPowerNode);
        powerNode.addChild(revokePowerNode);

        resourceNode.addChild(getResourceNode);
        resourceNode.addChild(setResourceNode);

        testNode.addChild(powerNode);
        testNode.addChild(resourceNode);

        dispatcher.getRoot().addChild(testNode);
    }

    private static int grantPower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPITest.POWER);

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (attachment == null || !attachment.hasPower(power))
            attachment.addPower(power);
        else {
            context.getSource().sendFailure(Component.literal("Entity already has power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "'."));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Granted power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "' to entity."), true);
        return 1;
    }

    private static int revokePower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPITest.POWER);

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (attachment != null && attachment.hasPower(power))
            attachment.removePower(power);
        else {
            context.getSource().sendFailure(Component.literal("Entity does not have power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "'."));
            return 0;
        }

        if (attachment.totalPowers() == 0)
            EffectAPITest.getHelper().removePowerAttachment(entity);

        context.getSource().sendSuccess(() -> Component.literal("Removed power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "' from entity."), true);
        return 1;
    }

    private static int setResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");

        ResourceEffect<Object> resource = DataResourceArgument.getResource(context, "key");
        Object value = DataResourceValueArgument.getResourceValue(context, "value");

        int successes = 0;

        for (Entity entity : entities) {
            if (EffectAPI.getHelper().getResources(entity) != null && EffectAPI.getHelper().getResources(entity).resources().containsKey(resource.getId())) {
                EffectAPI.getHelper().setResource(entity, resource.getId(), value);
                ++successes;
            }
        }

        if (successes == 0)
            context.getSource().sendFailure(Component.literal("None of the specified entities have the resource '" + resource.getId() + "'."));
        else if (successes == 1)
            context.getSource().sendSuccess(() -> Component.literal("Set resource '" + resource.getId() + "' to " + value + " on entity."), true);
        else if (successes > 1) {
            int finalSuccesses = successes;
            context.getSource().sendSuccess(() -> Component.literal("Set resource '" + resource.getId() + "' to " + value + " for " + finalSuccesses + " entities."), true);
        }

        return successes;
    }

    private static int getResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        ResourceEffect<Object> resource = DataResourceArgument.getResource(context, "key");

        if (EffectAPI.getHelper().getResources(entity) == null || !EffectAPI.getHelper().getResources(entity).resources().containsKey(resource.getId())) {
            context.getSource().sendFailure(Component.literal("Entity does not have resource '" + resource.getId() + "'."));
            return 0;
        }

        String value = EffectAPI.getHelper().getResources(entity).getValue(resource.getId()).toString();

        context.getSource().sendSuccess(() -> Component.literal("Resource '" + resource.getId() + "' is " + value + "."), true);
        return 1;
    }

    private static int removeResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");

        ResourceEffect<Object> resource = DataResourceArgument.getResource(context, "key");

        int successes = 0;

        for (Entity entity : entities) {
            if (EffectAPI.getHelper().getResources(entity) != null && EffectAPI.getHelper().getResources(entity).resources().containsKey(resource.getId())) {
                EffectAPI.getHelper().removeResource(entity, resource.getId());
                ++successes;
            }
        }

        if (successes == 0)
            context.getSource().sendFailure(Component.literal("None of the specified entities have the resource '" + resource.getId() + "'."));
        else if (successes == 1)
            context.getSource().sendSuccess(() -> Component.literal("Removed resource '" + resource.getId() + "' from entity."), true);
        else if (successes > 1) {
            int finalSuccesses = successes;
            context.getSource().sendSuccess(() -> Component.literal("Removed resource '" + resource.getId() + "' from " + finalSuccesses + " entities."), true);
        }

        return successes;
    }
}