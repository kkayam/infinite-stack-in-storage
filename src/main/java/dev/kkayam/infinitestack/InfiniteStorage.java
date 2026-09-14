package dev.kkayam.infinitestack;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Central rules for which inventories stack without limit and how much may leave them at once.
 * <p>
 * An inventory is "infinite storage" when its {@link Inventory#getMaxCountPerStack()} reports
 * {@link #INFINITE}. Vanilla and modded lootable containers get that from a mixin driven by
 * {@link StorageConfig}; the ender chest is opted in directly; the client's placeholder inventories
 * are flagged by a packet from the server when a screen opens. {@code DoubleInventory} delegates to
 * its halves, so double chests are covered automatically.
 */
public final class InfiniteStorage {
    public static final int INFINITE = Integer.MAX_VALUE;

    private InfiniteStorage() {}

    public static boolean isInfinite(Inventory inventory) {
        return inventory != null && inventory.getMaxCountPerStack() == INFINITE;
    }

    public static boolean isInfinite(Slot slot) {
        return slot != null && isInfinite(slot.inventory);
    }

    /** Config decision for a block-entity container; used by the LootableContainerBlockEntity mixin. */
    public static boolean isInfiniteContainer(BlockEntity blockEntity) {
        return StorageConfig.get().isInfinite(blockEntity.getType(), blockEntity instanceof LootableContainerBlockEntity);
    }

    /** How many of this stack fit in one slot outside storage: vanilla's rule. */
    public static int normalMax(ItemStack stack) {
        return stack.isStackable() ? stack.getMaxCount() : 1;
    }

    /** Per-slot limit for a given stack: unlimited in storage, vanilla elsewhere. */
    public static int slotLimit(Slot slot, ItemStack stack) {
        return isInfinite(slot) ? INFINITE : Math.min(slot.getMaxItemCount(stack), normalMax(stack));
    }

    /** Compact label for slot rendering: 999, 1.5k, 15k, 150k, 1.5M, ... */
    public static String abbreviate(int count) {
        if (count < 1000) return Integer.toString(count);
        String[] suffixes = {"k", "M", "B"};
        double value = count;
        int index = -1;
        while (value >= 1000 && index < suffixes.length - 1) {
            value /= 1000;
            index++;
        }
        if (value < 10) {
            String s = String.format("%.1f", value);
            if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
            return s + suffixes[index];
        }
        return (int) value + suffixes[index];
    }
}
