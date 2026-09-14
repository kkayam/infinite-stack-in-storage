package dev.kkayam.storagesolution.mixin.client;

import dev.kkayam.storagesolution.InfiniteStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Slot count labels above 999 are abbreviated (1.5k, 20k, 1.2M) so they fit in a 16px slot. */
@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @ModifyVariable(
            method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            at = @At("HEAD"), argsOnly = true)
    private String storagesolution$abbreviateCount(String countOverride, TextRenderer textRenderer, ItemStack stack) {
        if (countOverride == null && stack.getCount() > 999) {
            return InfiniteStorage.abbreviate(stack.getCount());
        }
        return countOverride;
    }
}
