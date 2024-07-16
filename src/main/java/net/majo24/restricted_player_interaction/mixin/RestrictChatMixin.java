package net.majo24.restricted_player_interaction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.majo24.restricted_player_interaction.RestrictedPlayerInteraction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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

    @WrapOperation(
            method = "onChatMessage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private CompletableFuture restrictChat(MinecraftServer instance, Runnable runnable, Operation<CompletableFuture> original) {
        if (!configManager.restrictChat() || playerHasPermission(this.player)) {
            original.call(instance, runnable);
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
}
