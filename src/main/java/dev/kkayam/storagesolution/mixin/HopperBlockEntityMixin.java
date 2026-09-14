package dev.kkayam.storagesolution.mixin;

import dev.kkayam.storagesolution.InfiniteStorage;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets hoppers (and droppers, which share this code) keep feeding an infinite stack instead of stopping at 64. */
@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    private static final String TRANSFER = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;"
            + "Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;";

    @Shadow
    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        throw new AssertionError();
    }

    @Inject(method = "isInventoryFull", at = @At("HEAD"), cancellable = true)
    private static void storagesolution$storageIsNeverFull(Inventory inventory, Direction direction,
                                                           CallbackInfoReturnable<Boolean> cir) {
        if (InfiniteStorage.isInfinite(inventory)) cir.setReturnValue(false);
    }

    @Redirect(method = TRANSFER, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/entity/HopperBlockEntity;canMergeItems(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private static boolean storagesolution$canMerge(ItemStack existing, ItemStack incoming,
                                                    Inventory from, Inventory to, ItemStack stack, int slot, Direction side) {
        return InfiniteStorage.isInfinite(to) ? ItemStack.canCombine(existing, incoming) : canMergeItems(existing, incoming);
    }

    @Redirect(method = TRANSFER, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxCount()I"))
    private static int storagesolution$mergeLimit(ItemStack stack,
                                                  Inventory from, Inventory to, ItemStack ignored, int slot, Direction side) {
        return InfiniteStorage.isInfinite(to) ? InfiniteStorage.INFINITE : stack.getMaxCount();
    }
}
