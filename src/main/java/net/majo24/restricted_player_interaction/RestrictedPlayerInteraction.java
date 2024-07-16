package net.majo24.restricted_player_interaction;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.majo24.restricted_player_interaction.config.ConfigFileHandler;
import net.majo24.restricted_player_interaction.config.ConfigManager;
import net.majo24.restricted_player_interaction.config.commands.Commands;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class RestrictedPlayerInteraction implements ModInitializer {
	public static final String MOD_ID = "restricted_player_interaction";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ConfigManager configManager;
	@Override
	public void onInitialize() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".toml");
		configManager = new ConfigManager(ConfigFileHandler.getConfigFromFile(configPath), configPath);

		Commands.registerCommands();
	}

	public static boolean playerHasPermission(ServerPlayerEntity player) {
		return (player.hasPermissionLevel(configManager.getPermissionLevel()));
	}
}