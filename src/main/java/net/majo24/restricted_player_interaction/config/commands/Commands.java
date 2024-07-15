package net.majo24.restricted_player_interaction.config.commands;

import static net.minecraft.server.command.CommandManager.*;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static net.majo24.restricted_player_interaction.RestrictedPlayerInteraction.configManager;

public class Commands {
    private Commands() {
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            final LiteralArgumentBuilder<ServerCommandSource> command = literal("restricted_player_interaction")
                    .requires((source -> environment.integrated || source.hasPermissionLevel(3)))
                    .then(restrictPlayerListLiteral())
                    .then(restrictChatLiteral())
                    .then(restrictCommandsLiteral())
                    .then(restrictJoinLeaveMessagesLiteral())
                    .then(permissionLevelLiteral());

            dispatcher.register(command);
        }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> restrictPlayerListLiteral() {
        return restrictLiteral("Restrict Player List", () -> configManager.restrictPlayerList(), configManager::setRestrictPlayerList);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> restrictChatLiteral() {
        return restrictLiteral("Restrict Chat", () -> configManager.restrictChat(), configManager::setRestrictChat);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> restrictCommandsLiteral() {
        return restrictLiteral("Restrict Commands", () -> configManager.restrictCommands(), configManager::setRestrictCommands);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> restrictJoinLeaveMessagesLiteral() {
        return restrictLiteral("Restrict Join/Leave Messages", () -> configManager.restrictJoinLeaveMessages(), configManager::setRestrictJoinLeaveMessages);
    }

    private static LiteralArgumentBuilder<ServerCommandSource> permissionLevelLiteral() {
        return literal("permission_level")
                .executes((context -> {
                    context.getSource().sendFeedback(() -> Text.literal("Permission level: " + configManager.getPermissionLevel()), false);
                    return 1;
                }))
                .then(argument("Permission Level", IntegerArgumentType.integer(0, 4))
                        .executes(context -> {
                            int permissionLevel = IntegerArgumentType.getInteger(context, "Permission Level");
                            configManager.setPermissionLevel(permissionLevel);
                            configManager.saveConfig();

                            context.getSource().sendFeedback(() -> Text.literal("Permission level has been set to " + permissionLevel + ". Requested by " + context.getSource().getName()), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> restrictLiteral(String optionName, BooleanSupplier get, Consumer<Boolean> set) {
        String literalName = optionName.toLowerCase().replace(" ", "_").replace("/", "_");

        return literal(literalName)
                .executes((context -> {
                    if (get.getAsBoolean()) {
                        context.getSource().sendFeedback(() -> Text.literal(optionName + " is enabled."), false);
                    } else {
                        context.getSource().sendFeedback(() -> Text.literal(optionName + " is not enabled."), false);
                    }
                    return 1;
                }))
                .then(argument(optionName, BoolArgumentType.bool())
                        .executes((context ->
                        {
                            boolean restrict = BoolArgumentType.getBool(context, optionName);
                            set.accept(restrict);
                            configManager.saveConfig();

                            if (get.getAsBoolean()) {
                                context.getSource().sendFeedback(() -> Text.literal(optionName + " has been set to true. Requested by " + context.getSource().getName()), true);
                            } else {
                                context.getSource().sendFeedback(() -> Text.literal(optionName + " has been set to false. Requested by " + context.getSource().getName()), true);
                            }
                            return 1;
                        })));
    }
}
