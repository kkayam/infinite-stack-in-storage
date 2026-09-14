package dev.kkayam.storagesolution.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla sends the stack count over the network as one signed byte. Counts above 127 are sent as
 * the marker byte -1 followed by a VarInt. Both sides must run this mod (it is required anyway).
 */
@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {
    private static final int BIG_COUNT_MARKER = -1;

    @Shadow public abstract int readVarInt();
    @Shadow public abstract PacketByteBuf writeVarInt(int value);

    @Redirect(method = "writeItemStack", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/PacketByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
    private io.netty.buffer.ByteBuf storagesolution$writeCount(PacketByteBuf buf, int count) {
        if (count > Byte.MAX_VALUE) {
            buf.writeByte(BIG_COUNT_MARKER);
            return this.writeVarInt(count);
        }
        return buf.writeByte(count);
    }

    @ModifyVariable(method = "readItemStack", at = @At(value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/network/PacketByteBuf;readByte()B"))
    private int storagesolution$readCount(int count) {
        return count == BIG_COUNT_MARKER ? this.readVarInt() : count;
    }
}
