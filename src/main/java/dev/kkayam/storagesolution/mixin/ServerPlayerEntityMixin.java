package dev.kkayam.storagesolution.mixin;

import dev.kkayam.storagesolution.StorageNetworking;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tells the client which slots are infinite storage, after the open-screen packet and before the contents are synced. */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void storagesolution$announceInfiniteSlots(ScreenHandler handler, CallbackInfo ci) {
        StorageNetworking.sendInfiniteSlots((ServerPlayerEntity) (Object) this, handler);
    }
}
