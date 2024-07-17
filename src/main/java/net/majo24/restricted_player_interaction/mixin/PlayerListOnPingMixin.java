package net.majo24.restricted_player_interaction.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.majo24.restricted_player_interaction.RestrictedPlayerInteraction;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerQueryNetworkHandler.class)
public class PlayerListOnPingMixin {
    @Shadow @Final private ServerMetadata metadata;

    @WrapOperation(
            method = "onRequest",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V"
            )
    )
    private void restrictPlayerListInMultiplayerScreen(ClientConnection instance, Packet<?> packet, Operation<Void> original) {
        if (RestrictedPlayerInteraction.configManager.restrictPlayerList()) {
            original.call(instance, new QueryResponseS2CPacket(new ServerMetadata(metadata.description(), Optional.empty(), metadata.version(), metadata.favicon(), metadata.secureChatEnforced())));
        } else {
            original.call(instance, packet);
        }
    }
}
