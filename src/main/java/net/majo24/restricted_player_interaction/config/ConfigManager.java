package net.majo24.restricted_player_interaction.config;

import java.nio.file.Path;

public class ConfigManager {
    public static final boolean DEFAULT_RESTRICT_PLAYER_LIST = true;
    public static final boolean DEFAULT_RESTRICT_CHAT = true;
    public static final boolean DEFAULT_RESTRICT_COMMANDS = true;
    public static final boolean DEFAULT_RESTRICT_JOIN_LEAVE_MESSAGES = true;
    public static final int DEFAULT_PERMISSION_LEVEL = 1;

    private final Config config;
    public final ConfigFileHandler fileHandler;

    public ConfigManager(Config config, Path configPath) {
        this.config = config;
        this.fileHandler = new ConfigFileHandler(configPath);
    }

    public static Config getDefaultConfig() {
        return new Config(DEFAULT_RESTRICT_PLAYER_LIST, DEFAULT_RESTRICT_CHAT,
                DEFAULT_RESTRICT_COMMANDS, DEFAULT_RESTRICT_JOIN_LEAVE_MESSAGES,
                DEFAULT_PERMISSION_LEVEL);
    }

    public void saveConfig() {
        this.fileHandler.saveConfig(this.config);
    }

    public boolean restrictPlayerList() {return config.restrictPlayerList();}

    public void setRestrictPlayerList(boolean restrictTabList) {config.setRestrictPlayerList(restrictTabList);}

    public boolean restrictChat() {return config.restrictChat();}

    public void setRestrictChat(boolean restrictChat) {config.setRestrictChat(restrictChat);}

    public boolean restrictCommands() {return config.restrictCommands();}

    public void setRestrictCommands(boolean restrictCommands) {config.setRestrictCommands(restrictCommands);}

    public boolean restrictJoinLeaveMessages() {return config.restrictJoinLeaveMessages();}

    public void setRestrictJoinLeaveMessages(boolean restrictJoinLeaveMessages) {config.setRestrictJoinLeaveMessages(restrictJoinLeaveMessages);}

    public int getPermissionLevel() {return config.getPermissionLevel();}

    public void setPermissionLevel(int permissionLevel) {config.setPermissionLevel(permissionLevel);}
}
