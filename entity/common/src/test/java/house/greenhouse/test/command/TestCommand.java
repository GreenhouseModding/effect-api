package house.greenhouse.test.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import house.greenhouse.effectapi.entity.api.EntityResourceAPI;
import house.greenhouse.effectapi.entity.api.command.EntityResourceArgument;
import house.greenhouse.effectapi.entity.api.command.EntityResourceValueArgument;
import house.greenhouse.effectapi.entity.impl.EffectAPIEntity;
import house.greenhouse.effectapi.entity.impl.effect.EntityResourceEffect;
import house.greenhouse.test.EffectAPIEntityTest;
import house.greenhouse.test.Power;
import house.greenhouse.test.attachment.PowersAttachment;
import io.netty.util.internal.ResourcesUtil;
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
                        .then(Commands.argument("power", ResourceArgument.resource(context, EffectAPIEntityTest.POWER))
                                .executes(TestCommand::addPower)))
                .build();
        LiteralCommandNode<CommandSourceStack> removePowerNode = Commands
                .literal("remove")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("power", ResourceArgument.resource(context, EffectAPIEntityTest.POWER))
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
                        .then(Commands.argument("key", EntityResourceArgument.resource("targets", "effectapi resource set", EffectAPIEntityTest.MOD_ID))
                                .then(Commands.argument("value", EntityResourceValueArgument.value("key","effectapi resource set <targets>"))
                                    .executes(TestCommand::setResource))))
                .build();
        LiteralCommandNode<CommandSourceStack> getResourceNode = Commands
                .literal("get")
                .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("key", EntityResourceArgument.resource("target", "effectapi resource get", EffectAPIEntityTest.MOD_ID))
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

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPIEntityTest.POWER);

        PowersAttachment attachment = EffectAPIEntityTest.getHelper().getPowers(entity);
        if (attachment != null && attachment.hasPower(power)) {
            context.getSource().sendFailure(Component.literal("Entity already has power \"" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "\"."));
            return 0;
        }
        attachment.addPower(power);
        attachment.sync();

        context.getSource().sendSuccess(() -> Component.literal("Added power \"" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "\" to entity."), true);
        return 1;
    }

    private static int removePower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPIEntityTest.POWER);

        PowersAttachment attachment = EffectAPIEntityTest.getHelper().getPowers(entity);
        if (attachment == null || !attachment.hasPower(power)) {
            context.getSource().sendFailure(Component.literal("Entity does not have power \"" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "\"."));
            return 0;
        }
        attachment.removePower(power);
        attachment.sync();

        if (attachment.isEmpty())
            EffectAPIEntityTest.getHelper().removePowerAttachment(entity);

        context.getSource().sendSuccess(() -> Component.literal("Removed power \"" + power.unwrapKey().map(ResourceKey::location).orElse(null) + "\" from entity."), true);
        return 1;
    }

    private static int listPowers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        if (!EffectAPIEntityTest.getHelper().hasPowers(entity)) {
            context.getSource().sendFailure(Component.literal("Entity does not have any powers."));
            return 0;
        }

        PowersAttachment attachment = EffectAPIEntityTest.getHelper().getPowers(entity);
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

        PowersAttachment attachment = EffectAPIEntityTest.getHelper().getPowers(entity);
        if (attachment == null) {
            context.getSource().sendFailure(Component.literal("Entity does not have any powers."));
            return 0;
        }

        EffectAPIEntityTest.getHelper().removePowerAttachment(entity);

        context.getSource().sendSuccess(() -> Component.literal("Removed all powers from entity."), true);
        return 1;
    }

    private static int setResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");

        EntityResourceEffect<Object> resource = EntityResourceArgument.getResource(context, "key");
        Object value = EntityResourceValueArgument.getResourceValue(context, "value");

        int successes = 0;

        for (Entity entity : entities) {
            if (EffectAPIEntity.getHelper().getResources(entity) != null && EffectAPIEntity.getHelper().getResources(entity).hasResource(resource.getId())) {
                EntityResourceAPI.setResourceValue(entity, resource.getId(), value, EffectAPIEntityTest.POWER.location());
                ++successes;
            }
        }

        if (successes == 0)
            context.getSource().sendFailure(Component.literal("None of the specified entities have the resource \"" + resource.getId() + "\"."));
        else if (successes == 1)
            context.getSource().sendSuccess(() -> Component.literal("Set resource \"" + resource.getId() + "\" to " + value + " on entity."), true);
        else if (successes > 1) {
            int finalSuccesses = successes;
            context.getSource().sendSuccess(() -> Component.literal("Set resource \"" + resource.getId() + "\" to " + value + " for " + finalSuccesses + " entities."), true);
        }

        return successes;
    }

    private static int getResource(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        EntityResourceEffect<Object> resource = EntityResourceArgument.getResource(context, "key");

        if (EffectAPIEntity.getHelper().getResources(entity) == null || !EffectAPIEntity.getHelper().getResources(entity).hasResource(resource.getId())) {
            context.getSource().sendFailure(Component.literal("Entity does not have resource \"" + resource.getId() + "\"."));
            return 0;
        }

        Object value = EntityResourceAPI.getResourceValue(entity, resource.getId());
        String stringValue = value instanceof String str ? "\"" + str + "\"" : value.toString();

        context.getSource().sendSuccess(() -> Component.literal("Resource \"" + resource.getId() + "\" is " + stringValue + "."), true);
        if (value instanceof Number number)
            return number.intValue();
        if (value instanceof Boolean bool)
            return bool.booleanValue() ? 1 : 0;

        return 1;
    }
}