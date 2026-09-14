package dev.kkayam.storagesolution.mixin;

import dev.kkayam.storagesolution.InfiniteInventory;
import dev.kkayam.storagesolution.InfiniteStorage;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * The client never sees the real chest block entity: its screen handler wraps a plain
 * SimpleInventory, whose setStack() clamps counts to 64. Flagged instances report the
 * infinite limit instead so client prediction and rendering match the server.
 */
@Mixin(SimpleInventory.class)
public abstract class SimpleInventoryMixin implements InfiniteInventory, Inventory {
    @Unique private boolean storagesolution$infinite;

    @Override
    public void storagesolution$markInfinite() {
        this.storagesolution$infinite = true;
    }

    @Override
    public int getMaxCountPerStack() {
        return this.storagesolution$infinite ? InfiniteStorage.INFINITE : Inventory.MAX_COUNT_PER_STACK;
    }
}
