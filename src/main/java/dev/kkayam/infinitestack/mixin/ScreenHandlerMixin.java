package dev.kkayam.infinitestack.mixin;

import dev.kkayam.infinitestack.InfiniteStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow @Final public DefaultedList<Slot> slots;
    @Shadow public abstract ItemStack getCursorStack();

    /**
     * Replacement for vanilla's shift-click merge. Vanilla caps every merge at the item's own max
     * count and refuses to merge non-stackable items at all. Here each slot's limit comes from
     * {@link InfiniteStorage#slotLimit}: unlimited in storage, vanilla everywhere else. Anything
     * that cannot fit stays in {@code stack}.
     */
    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void infinitestack$insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast,
                                            CallbackInfoReturnable<Boolean> cir) {
        boolean moved = false;

        // Pass 1: top up slots that already hold an identical item.
        for (int i = fromLast ? endIndex - 1 : startIndex;
             !stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex);
             i += fromLast ? -1 : 1) {
            Slot slot = this.slots.get(i);
            ItemStack existing = slot.getStack();
            if (existing.isEmpty() || !ItemStack.canCombine(stack, existing)) continue;
            int room = InfiniteStorage.slotLimit(slot, stack) - existing.getCount();
            if (room <= 0) continue;
            int n = Math.min(room, stack.getCount());
            existing.increment(n);
            stack.decrement(n);
            slot.markDirty();
            moved = true;
        }

        // Pass 2: fill empty slots.
        for (int i = fromLast ? endIndex - 1 : startIndex;
             !stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex);
             i += fromLast ? -1 : 1) {
            Slot slot = this.slots.get(i);
            if (!slot.getStack().isEmpty() || !slot.canInsert(stack)) continue;
            int n = Math.min(InfiniteStorage.slotLimit(slot, stack), stack.getCount());
            slot.setStack(stack.split(n));
            slot.markDirty();
            moved = true;
        }

        cir.setReturnValue(moved);
    }

    /**
     * Guards the two vanilla paths that would move an oversized storage stack somewhere else whole:
     * swapping the cursor with a slot, and hotbar-swapping (number keys / F).
     */
    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void infinitestack$guardOversizedMoves(int slotIndex, int button, SlotActionType actionType,
                                                     PlayerEntity player, CallbackInfo ci) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        Slot slot = this.slots.get(slotIndex);
        if (!InfiniteStorage.isInfinite(slot) || !slot.hasStack()) return;
        ItemStack stored = slot.getStack();
        int normalMax = InfiniteStorage.normalMax(stored);
        if (stored.getCount() <= normalMax) return;

        if (actionType == SlotActionType.PICKUP) {
            ItemStack cursor = this.getCursorStack();
            if (!cursor.isEmpty() && !ItemStack.canCombine(stored, cursor)) {
                ci.cancel(); // would swap the whole oversized stack onto the cursor
            }
        } else if (actionType == SlotActionType.SWAP) {
            ItemStack hotbar = player.getInventory().getStack(button);
            if (hotbar.isEmpty() && slot.canTakeItems(player)) {
                ItemStack taken = slot.takeStack(normalMax);
                player.getInventory().setStack(button, taken);
                slot.onTakeItem(player, taken);
            }
            ci.cancel(); // never swap an oversized stack into the player inventory whole
        }
    }
}
