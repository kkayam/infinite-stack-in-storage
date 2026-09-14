package dev.kkayam.infinitestack.mixin;

import dev.kkayam.infinitestack.InfiniteInventory;
import dev.kkayam.infinitestack.InfiniteStorage;
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
    @Unique private boolean infinitestack$infinite;

    @Override
    public void infinitestack$markInfinite() {
        this.infinitestack$infinite = true;
    }

    @Override
    public int getMaxCountPerStack() {
        return this.infinitestack$infinite ? InfiniteStorage.INFINITE : Inventory.MAX_COUNT_PER_STACK;
    }
}
