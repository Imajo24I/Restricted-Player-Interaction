package net.majo24.restricted_player_interaction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.majo24.restricted_player_interaction.RestrictedPlayerInteraction;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

@Debug(export = true)
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    public abstract MinecraftServer getServer();

    @Shadow
    @Final
    private Map<UUID, ServerPlayerEntity> playerMap;

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V")
    )
    private void sendPlayerList(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
        if (packet instanceof PlayerListS2CPacket) {
            if (RestrictedPlayerInteraction.configManager.restrictPlayerList() && this.getServer().isRemote()
                    && !instance.player.hasPermissionLevel(RestrictedPlayerInteraction.configManager.getPermissionLevel())) {
                EnumSet<PlayerListS2CPacket.Action> enumSet = EnumSet.of(
                        PlayerListS2CPacket.Action.ADD_PLAYER,
                        PlayerListS2CPacket.Action.INITIALIZE_CHAT,
                        PlayerListS2CPacket.Action.UPDATE_GAME_MODE,
                        PlayerListS2CPacket.Action.UPDATE_LATENCY,
                        PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME
                );

                original.call(instance, new PlayerListS2CPacket(enumSet, this.players));
            }

        }
        original.call(instance, packet);
    }

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V")
    )
    private void sendPlayerToEveryone(PlayerManager instance, Packet<?> packet, Operation<Void> original) {
        if (RestrictedPlayerInteraction.configManager.restrictPlayerList() && this.getServer().isRemote()) {
            for (ServerPlayerEntity serverPlayerEntity : this.players) {
                if (serverPlayerEntity.hasPermissionLevel(RestrictedPlayerInteraction.configManager.getPermissionLevel())) {
                    serverPlayerEntity.networkHandler.sendPacket(packet);
                }
            }
        } else {
            original.call(instance, packet);
        }
    }

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V")
    )

    private void onlyBroadcastToOp(PlayerManager instance, Text message, boolean overlay, Operation<Void> original) {
        if (RestrictedPlayerInteraction.configManager.restrictJoinLeaveMessages()) {
            this.server.sendMessage(message);

            int requiredPermissionLevel = RestrictedPlayerInteraction.configManager.getPermissionLevel();
            for (ServerPlayerEntity serverPlayerEntity : this.players) {
                if (serverPlayerEntity.hasPermissionLevel(requiredPermissionLevel) || !this.getServer().isRemote()) {
                    serverPlayerEntity.sendMessageToClient(message, overlay);
                }
            }
        } else {
            original.call(instance, message, overlay);
        }
    }
}
