package dev.kkayam.storagesolution;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * The client never sees the real container; its screen handler wraps a placeholder inventory that
 * clamps counts to 64. When a screen opens, the server tells the client which slots belong to
 * infinite storage so the client flags those placeholders (see SimpleInventoryMixin). Sent from
 * ServerPlayerEntity.onScreenHandlerOpened, i.e. after the open-screen packet and before the contents.
 */
public final class StorageNetworking {
    public static final Identifier INFINITE_SLOTS = new Identifier(StorageSolution.MOD_ID, "infinite_slots");

    private StorageNetworking() {}

    public static void sendInfiniteSlots(ServerPlayerEntity player, ScreenHandler handler) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < handler.slots.size(); i++) {
            if (InfiniteStorage.isInfinite(handler.slots.get(i))) slots.add(i);
        }
        if (slots.isEmpty() || !ServerPlayNetworking.canSend(player, INFINITE_SLOTS)) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(handler.syncId);
        buf.writeVarInt(slots.size());
        for (int slot : slots) buf.writeVarInt(slot);
        ServerPlayNetworking.send(player, INFINITE_SLOTS, buf);
    }
}
