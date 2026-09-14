package dev.kkayam.infinitestack.mixin;

import dev.kkayam.infinitestack.InfiniteStorage;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/** Marks the ender chest as infinite storage and disables the setStack() count clamp in SimpleInventory. */
@Mixin(EnderChestInventory.class)
public abstract class EnderChestInventoryMixin implements Inventory {
    @Override
    public int getMaxCountPerStack() {
        return InfiniteStorage.INFINITE;
    }
}
