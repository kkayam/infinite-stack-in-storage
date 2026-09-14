package dev.kkayam.infinitestack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.NumberFormat;

public final class InfiniteStackClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Slot labels are abbreviated above 999 (see DrawContextMixin); show the exact count in the tooltip.
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (stack.getCount() > 999) {
                lines.add(Text.literal("Count: " + NumberFormat.getIntegerInstance().format(stack.getCount()))
                        .formatted(Formatting.GRAY));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(StorageNetworking.INFINITE_SLOTS, (client, handler, buf, responseSender) -> {
            int syncId = buf.readVarInt();
            int count = buf.readVarInt();
            int[] slots = new int[count];
            for (int i = 0; i < count; i++) slots[i] = buf.readVarInt();
            client.execute(() -> {
                if (client.player == null) return;
                ScreenHandler screenHandler = client.player.currentScreenHandler;
                if (screenHandler == null || screenHandler.syncId != syncId) return;
                for (int index : slots) {
                    if (index < 0 || index >= screenHandler.slots.size()) continue;
                    Slot slot = screenHandler.slots.get(index);
                    if (!(slot.inventory instanceof PlayerInventory) && slot.inventory instanceof InfiniteInventory infinite) {
                        infinite.infinitestack$markInfinite();
                    }
                }
            });
        });
    }
}
