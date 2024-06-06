package dev.greenhouseteam.test.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.greenhouseteam.effectapi.api.entity.command.EntityResourceArgument;
import dev.greenhouseteam.effectapi.api.entity.command.EntityResourceValueArgument;
import dev.greenhouseteam.effectapi.impl.entity.EffectAPIEntity;
import dev.greenhouseteam.effectapi.impl.entity.effect.EntityResourceEffect;
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

        LiteralCommandNode<CommandSourceStack> addPowerNode = Commands
                .literal("add")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("power", ResourceArgument.resource(context, EffectAPITest.POWER))
                                .executes(TestCommand::addPower)))
                .build();
        LiteralCommandNode<CommandSourceStack> removePowerNode = Commands
                .literal("remove")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("power", ResourceArgument.resource(context, EffectAPITest.POWER))
                                .executes(TestCommand::removePower)))
                .build();
        LiteralCommandNode<CommandSourceStack> listPowersNode = Commands
                .literal("list")
                .then(Commands.argument("target", EntityArgument.entity())
                                .executes(TestCommand::listPowers))
                .build();
        LiteralCommandNode<CommandSourceStack> clearPowersNode = Commands
                .literal("clear")
                .then(Commands.argument("target", EntityArgument.entity())
                        .executes(TestCommand::clearPowers))
                .build();

        LiteralCommandNode<CommandSourceStack> setResourceNode = Commands
                .literal("set")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("key", EntityResourceArgument.resource("targets", "effectapi resource set", PowersAttachment.ID))
                                .then(Commands.argument("value", EntityResourceValueArgument.value("key","effectapi resource set <targets>"))
                                    .executes(TestCommand::setResource))))
                .build();
        LiteralCommandNode<CommandSourceStack> getResourceNode = Commands
                .literal("get")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("key", EntityResourceArgument.resource("target", "effectapi resource get", PowersAttachment.ID))
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

    private static int addPower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPITest.POWER);

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (attachment != null && attachment.hasPower(power)) {
            context.getSource().sendFailure(Component.literal("Entity already has power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "'."));
            return 0;
        }
        attachment.addPower(power);

        context.getSource().sendSuccess(() -> Component.literal("Added power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "' to entity."), true);
        return 1;
    }

    private static int removePower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPITest.POWER);

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (attachment == null || !attachment.hasPower(power)) {
            context.getSource().sendFailure(Component.literal("Entity does not have power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "'."));
            return 0;
        }

        attachment.removePower(power);

        if (attachment.totalPowers() == 0)
            EffectAPITest.getHelper().removePowerAttachment(entity);

        context.getSource().sendSuccess(() -> Component.literal("Removed power '" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "' from entity."), true);
        return 1;
    }

    private static int listPowers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
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


    private static int clearPowers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (attachment == null) {
            context.getSource().sendFailure(Component.literal("Entity does not have any powers."));
            return 0;
        }

        EffectAPITest.getHelper().removePowerAttachment(entity);

        context.getSource().sendSuccess(() -> Component.literal("Removed all powers from entity."), true);
        return 1;
    }

    private static int setResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");

        EntityResourceEffect<Object> resource = EntityResourceArgument.getResource(context, "key");
        Object value = EntityResourceValueArgument.getResourceValue(context, "value");

        int successes = 0;

        for (Entity entity : entities) {
            if (EffectAPIEntity.getHelper().getResources(entity) != null && EffectAPIEntity.getHelper().getResources(entity).resources().containsKey(resource.getId())) {
                EffectAPIEntity.getHelper().setResource(entity, resource.getId(), value, null);
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

        EntityResourceEffect<Object> resource = EntityResourceArgument.getResource(context, "key");

        if (EffectAPIEntity.getHelper().getResources(entity) == null || !EffectAPIEntity.getHelper().getResources(entity).resources().containsKey(resource.getId())) {
            context.getSource().sendFailure(Component.literal("Entity does not have resource '" + resource.getId() + "'."));
            return 0;
        }

        Object value = EffectAPIEntity.getHelper().getResources(entity).getValue(resource.getId());

        context.getSource().sendSuccess(() -> Component.literal("Resource '" + resource.getId() + "' is " + value.toString() + "."), true);
        if (value instanceof Number number)
            return number.intValue();
        if (value instanceof Boolean bool)
            return bool.booleanValue() ? 1 : 0;

        return 1;
    }
}