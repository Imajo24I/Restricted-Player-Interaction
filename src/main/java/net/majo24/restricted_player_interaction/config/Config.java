package net.majo24.restricted_player_interaction.config;

public class Config {
    private boolean restrictPlayerList;
    private boolean restrictChat;
    private boolean restrictCommands;
    private boolean restrictJoinLeaveMessages;

    private int permissionLevel;

    public Config(boolean restrictPlayerList, boolean restrictChat, boolean restrictCommands, boolean restrictJoinLeaveMessages, int permissionLevel) {
        this.restrictPlayerList = restrictPlayerList;
        this.restrictChat = restrictChat;
        this.restrictCommands = restrictCommands;
        this.restrictJoinLeaveMessages = restrictJoinLeaveMessages;
        this.permissionLevel = permissionLevel;
    }

    public boolean restrictPlayerList() {
        return restrictPlayerList;
    }

    public void setRestrictPlayerList(boolean restrictPlayerList) {
        this.restrictPlayerList = restrictPlayerList;
    }

    public boolean restrictChat() {
        return restrictChat;
    }

    public void setRestrictChat(boolean restrictChat) {
        this.restrictChat = restrictChat;
    }

    public boolean restrictCommands() {
        return restrictCommands;
    }

    public void setRestrictCommands(boolean restrictCommands) {
        this.restrictCommands = restrictCommands;
    }

    public boolean restrictJoinLeaveMessages() {
        return restrictJoinLeaveMessages;
    }

    public void setRestrictJoinLeaveMessages(boolean restrictJoinLeaveMessages) {
        this.restrictJoinLeaveMessages = restrictJoinLeaveMessages;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }
}
