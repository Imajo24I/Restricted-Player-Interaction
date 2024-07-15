package net.majo24.restricted_player_interaction.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.majo24.restricted_player_interaction.RestrictedPlayerInteraction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

public record ConfigFileHandler(Path configPath) {

    public static Config getConfigFromFile(Path configPath) {
        if (Files.exists(configPath)) {
            // Get config from file
            try {
                CommentedFileConfig fileConfig = CommentedFileConfig.of(configPath.toFile());
                fileConfig.load();
                return configFromFileConfig(fileConfig);
            } catch (Exception e) {
                invalidConfigCrash(e, configPath);
                return ConfigManager.getDefaultConfig();
            }
        } else {
            // Create a new Config
            RestrictedPlayerInteraction.LOGGER.info("Creating Restricted Player Interaction config file");
            Config config = ConfigManager.getDefaultConfig();
            try {
                Files.createFile(configPath);
                CommentedFileConfig fileConfig = fileConfigFromConfig(config, configPath);
                fileConfig.save();
            } catch (Exception e) {
                RestrictedPlayerInteraction.LOGGER.error("Could not create Restricted Player Interaction config file. Using default config.", e);
            }
            return config;
        }
    }

    private static Config configFromFileConfig(CommentedFileConfig fileConfig) {
        try {
            com.electronwill.nightconfig.core.Config general = fileConfig.get("general");
            com.electronwill.nightconfig.core.Config restrictions = fileConfig.get("restrictions");

            int permissionLevel = getAndValidateConfigEntry("permission_level", () -> general.get("permission_level"), ConfigManager.DEFAULT_PERMISSION_LEVEL, fileConfig.getNioPath());

            boolean restrictPlayerList = getAndValidateConfigEntry("restrict_player_list", () -> restrictions.get("restrict_player_list"), ConfigManager.DEFAULT_RESTRICT_PLAYER_LIST, fileConfig.getNioPath());
            boolean restrictChat = getAndValidateConfigEntry("restrict_chat", () -> restrictions.get("restrict_chat"), ConfigManager.DEFAULT_RESTRICT_CHAT, fileConfig.getNioPath());
            boolean restrictCommands = getAndValidateConfigEntry("restrict_commands", () -> restrictions.get("restrict_commands"), ConfigManager.DEFAULT_RESTRICT_COMMANDS, fileConfig.getNioPath());
            boolean restrictJoinLeaveMessages = getAndValidateConfigEntry("restrict_join_leave_messages", () -> restrictions.get("restrict_join_leave_messages"), ConfigManager.DEFAULT_RESTRICT_JOIN_LEAVE_MESSAGES, fileConfig.getNioPath());

            return new Config(
                    restrictPlayerList, restrictChat, restrictCommands, restrictJoinLeaveMessages, permissionLevel
            );
        } catch (Exception e) {
            invalidConfigCrash(e, fileConfig.getNioPath());
            return ConfigManager.getDefaultConfig();
        }
    }

    private static <T> T getAndValidateConfigEntry(String configName, Supplier<T> supplier, T defaultValue, Path configPath) {
        try {
            return Objects.requireNonNull(supplier.get());
        } catch (Exception e) {
            RestrictedPlayerInteraction.LOGGER.error("Failed to load config option \"{}\" from Restricted Player Interaction config file. Using the default value \"{}\" for this session. Please ensure the entry and the config file are valid. You can reset the config file by deleting the file. It is located under \"{}\".", configName, defaultValue, configPath, e);
            return defaultValue;
        }
    }

    public void saveConfig(Config config) {
        RestrictedPlayerInteraction.LOGGER.info("Saving Restricted Player Interaction config to file");
        CommentedFileConfig fileConfig = fileConfigFromConfig(config, configPath);
        fileConfig.save();
    }

    private static CommentedFileConfig fileConfigFromConfig(Config config, Path configPath) {
        CommentedFileConfig fileConfig = CommentedFileConfig.of(new File(configPath.toString()));

        // General Subcategory
        CommentedConfig general = fileConfig.createSubConfig();
        CommentedConfig restrictions = fileConfig.createSubConfig();

        restrictions.add("restrict_player_list", config.restrictPlayerList());
        restrictions.setComment("restrict_player_list", "Players without given permission level will have the tab list be empty.");

        restrictions.add("restrict_chat", config.restrictChat());
        restrictions.setComment("restrict_chat", "Players without given permission level will not be able to chat.");

        restrictions.add("restrict_commands", config.restrictCommands());
        restrictions.setComment("restrict_commands", "Players without given permission level will not be able to execute serverside commands.");

        restrictions.add("restrict_join_leave_messages", config.restrictJoinLeaveMessages());
        restrictions.setComment("restrict_join_leave_messages", "Players without given permission level will not be able to see join/leave messages.");

        general.add("permission_level", config.getPermissionLevel());
        general.setComment("permission_level", "The permission level required to bypass the restrictions.");

        // Add all subcategories to the main config
        fileConfig.add("general", general);
        fileConfig.setComment("general", "General Settings for the mod.");

        fileConfig.add("restrictions", restrictions);
        fileConfig.setComment("restrictions", "Available Restrictions.");
        return fileConfig;
    }

    private static void invalidConfigCrash(Exception e, Path configPath) {
        MinecraftClient.getInstance().setCrashReportSupplierAndAddDetails(new CrashReport("Failed to load Mob Armor Trims config from file.", new IllegalStateException("Failed to load Mob Armor Trims config from file. Please make sure your config file is valid. You can reset it by deleting the file. It is located under " + configPath + ".\n" + e.getMessage())));
    }
}
