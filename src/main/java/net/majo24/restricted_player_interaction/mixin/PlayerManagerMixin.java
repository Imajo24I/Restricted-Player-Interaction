package net.majo24.restricted_player_interaction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

import static net.majo24.restricted_player_interaction.RestrictedPlayerInteraction.configManager;
import static net.majo24.restricted_player_interaction.RestrictedPlayerInteraction.playerHasPermission;

@Debug(export = true)
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V")
    )
    private void sendPlayersWithoutPlayerListUpdate(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original) {
        if (packet instanceof PlayerListS2CPacket && configManager.restrictPlayerList()
                && !playerHasPermission(instance.player)) {
            EnumSet<PlayerListS2CPacket.Action> enumSet = EnumSet.of(
                    PlayerListS2CPacket.Action.ADD_PLAYER,
                    PlayerListS2CPacket.Action.INITIALIZE_CHAT,
                    PlayerListS2CPacket.Action.UPDATE_GAME_MODE,
                    PlayerListS2CPacket.Action.UPDATE_LATENCY,
                    PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME
            );

            original.call(instance, new PlayerListS2CPacket(enumSet, this.players));
            return;
        }

        original.call(instance, packet);
    }

    @WrapOperation(
            method = "onPlayerConnect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V")
    )
    private void sendPlayerToPermissioned(PlayerManager instance, Packet<?> packet, Operation<Void> original) {
        if (configManager.restrictPlayerList()) {
            for (ServerPlayerEntity player : this.players) {
                if (playerHasPermission(player)) {
                    player.networkHandler.sendPacket(packet);
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

    private void onlyBroadcastToPermissioned(PlayerManager instance, Text message, boolean overlay, Operation<Void> original) {
        if (configManager.restrictJoinLeaveMessages()) {
            this.server.sendMessage(message);

            for (ServerPlayerEntity player : this.players) {
                if (playerHasPermission(player)) {
                    player.sendMessageToClient(message, overlay);
                }
            }
        } else {
            original.call(instance, message, overlay);
        }
    }
}
