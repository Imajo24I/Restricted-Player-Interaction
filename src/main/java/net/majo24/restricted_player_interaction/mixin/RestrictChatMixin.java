package net.majo24.restricted_player_interaction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

import static net.majo24.restricted_player_interaction.RestrictedPlayerInteraction.configManager;
import static net.majo24.restricted_player_interaction.RestrictedPlayerInteraction.playerHasPermission;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class RestrictChatMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow @Final private MinecraftServer server;

    @WrapOperation(
            method = "onChatMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private CompletableFuture restrictChat(MinecraftServer instance, Runnable runnable, Operation<CompletableFuture> original) {
        if (!configManager.restrictChat() || playerHasPermission(this.player)) {
            return original.call(instance, runnable);
        }
        return null;
    }

    @WrapOperation(
            method = "onCommandExecution",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    public CompletableFuture restrictCommands(MinecraftServer instance, Runnable runnable, Operation<CompletableFuture> original) {
        if (!configManager.restrictCommands() || playerHasPermission(this.player)) {
            return original.call(instance, runnable);
        }
        return null;
    }

    @WrapOperation(
            method = "onDisconnected",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"
            )
    )
    private void onlyBroadcastLeaveToPermissioned(PlayerManager instance, Text message, boolean overlay, Operation<Void> original) {
        if (configManager.restrictJoinLeaveMessages()) {
            this.server.sendMessage(message);

            for (ServerPlayerEntity player : instance.getPlayerList()) {
                if (playerHasPermission(player)) {
                    player.sendMessageToClient(message, overlay);
                }
            }
        } else {
            original.call(instance, message, overlay);
        }
    }
}
