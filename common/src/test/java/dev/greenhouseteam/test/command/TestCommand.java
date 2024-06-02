package dev.greenhouseteam.test.command;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
import net.minecraft.world.entity.Entity;

public class TestCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {

        LiteralCommandNode<CommandSourceStack> testNode = Commands
                .literal("effectapi")
                .build();

        LiteralCommandNode<CommandSourceStack> powerNode = Commands
                .literal("power")
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

        powerNode.addChild(grantPowerNode);
        powerNode.addChild(revokePowerNode);

        testNode.addChild(powerNode);

        dispatcher.getRoot().addChild(testNode);
    }

    private static int grantPower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPITest.POWER);

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (!attachment.hasPower(power))
            attachment.addPower(power);

        return 1;
    }

    private static int revokePower(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");

        Holder<Power> power = ResourceArgument.getResource(context, "power", EffectAPITest.POWER);

        PowersAttachment attachment = EffectAPITest.getHelper().getPowers(entity);
        if (attachment.hasPower(power))
            attachment.removePower(power);
        if (attachment.totalPowers() == 0)
            EffectAPITest.getHelper().removePowerAttachment(entity);

        return 1;
    }
}