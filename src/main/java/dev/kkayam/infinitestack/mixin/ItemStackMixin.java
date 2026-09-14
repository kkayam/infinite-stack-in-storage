package dev.kkayam.infinitestack.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla saves the stack count as a single byte. Counts above 127 are saved in an extra int tag;
 * the byte tag keeps a normal-sized value so a world opened without this mod still loads cleanly
 * (with the excess lost rather than corrupted).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    private static final String BIG_COUNT = "infinitestack:count";
    /** Tag written by builds of this mod before it was renamed. */
    private static final String LEGACY_BIG_COUNT = "storagesolution:count";

    @Shadow private int count;
    @Shadow public abstract int getMaxCount();

    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void infinitestack$writeBigCount(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (this.count > Byte.MAX_VALUE) {
            nbt.putByte("Count", (byte) Math.min(this.count, this.getMaxCount()));
            nbt.putInt(BIG_COUNT, this.count);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("RETURN"))
    private void infinitestack$readBigCount(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(BIG_COUNT, NbtElement.INT_TYPE)) {
            this.count = nbt.getInt(BIG_COUNT);
        } else if (nbt.contains(LEGACY_BIG_COUNT, NbtElement.INT_TYPE)) {
            this.count = nbt.getInt(LEGACY_BIG_COUNT);
        }
    }
}
