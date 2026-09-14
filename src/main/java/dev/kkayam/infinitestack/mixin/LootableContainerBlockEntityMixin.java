package dev.kkayam.infinitestack.mixin;

import dev.kkayam.infinitestack.InfiniteStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Chests, barrels, shulker boxes and most modded chests extend this class. Reporting the infinite
 * limit marks them as storage and disables the count clamp in setStack(). Which types qualify is
 * decided by StorageConfig (hoppers, dispensers and droppers are excluded by default).
 * Implements Inventory so the override is recognised and remapped in production.
 */
@Mixin(LootableContainerBlockEntity.class)
public abstract class LootableContainerBlockEntityMixin implements Inventory {
    @Override
    public int getMaxCountPerStack() {
        return InfiniteStorage.isInfiniteContainer((BlockEntity) (Object) this) ? InfiniteStorage.INFINITE : Inventory.MAX_COUNT_PER_STACK;
    }
}
