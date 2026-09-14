package dev.kkayam.storagesolution.mixin;

import dev.kkayam.storagesolution.InfiniteStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow @Final public Inventory inventory;
    @Shadow public abstract ItemStack getStack();

    /** Storage slots accept any amount of a stack, regardless of the item's own max count. */
    @Inject(method = "getMaxItemCount(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void storagesolution$unlimitedPerStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (InfiniteStorage.isInfinite(this.inventory)) {
            cir.setReturnValue(InfiniteStorage.INFINITE);
        }
    }

    /**
     * Taking from a storage slot (pick up, drop with Q / Ctrl+Q) yields at most a normal stack.
     * Covers takeStackRange() too, which delegates here.
     */
    @ModifyVariable(method = "tryTakeStackRange", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int storagesolution$capTakenAmount(int max, int min, int ignored, PlayerEntity player) {
        if (InfiniteStorage.isInfinite(this.inventory)) {
            return Math.min(max, InfiniteStorage.normalMax(this.getStack()));
        }
        return max;
    }
}
