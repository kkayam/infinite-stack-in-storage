package dev.kkayam.infinitestack.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/** Drives the real ScreenHandler click logic against a real chest, exactly as the server does for a player. */
public final class ClickTests {
    private static final int PLAYER_FIRST_SLOT = 27; // handler slots 0-26 are the chest

    public record Setup(Inventory chest, PlayerEntity player, GenericContainerScreenHandler handler) {}

    public static Setup setup(ServerWorld world, BlockPos pos) {
        return setup(world, pos, Blocks.CHEST);
    }

    /** Places the block and returns its inventory without opening a screen handler (any size), or null if not an Inventory. */
    public static Setup setupBlockOnly(ServerWorld world, BlockPos pos, Block block) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.setBlockState(pos, block.getDefaultState());
        return world.getBlockEntity(pos) instanceof Inventory inv ? new Setup(inv, null, null) : null;
    }

    /** Works for any container block whose block entity is an Inventory with at least 27 slots. */
    public static Setup setup(ServerWorld world, BlockPos pos, Block block) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
        world.setBlockState(pos, block.getDefaultState());
        Inventory chest = (Inventory) world.getBlockEntity(pos);
        PlayerEntity player = new PlayerEntity(world, BlockPos.ORIGIN, 0.0F, new GameProfile(UUID.randomUUID(), "sstest-player")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
        };
        GenericContainerScreenHandler handler = GenericContainerScreenHandler.createGeneric9x3(1, player.getInventory(), chest);
        return new Setup(chest, player, handler);
    }

    public static void expect(String what, int actual, int expected) {
        if (actual != expected) throw new IllegalStateException(what + ": expected " + expected + " but was " + actual);
    }

    public static final List<String> NAMES = List.of("clickMergesBeyond64", "shiftClickIntoChestMerges", "takingOutGivesNormalStacks", "identicalSwordsStack");
    public static final List<Consumer<Setup>> TESTS = List.of(ClickTests::clickMergesBeyond64, ClickTests::shiftClickIntoChestMerges, ClickTests::takingOutGivesNormalStacks, ClickTests::identicalSwordsStack);

    /** Runs every test against a fresh chest at pos; returns one PASS/FAIL line per test. */
    public static List<String> runAll(ServerWorld world, BlockPos pos, Block block) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < TESTS.size(); i++) {
            try {
                TESTS.get(i).accept(setup(world, pos, block));
                out.add("PASS " + NAMES.get(i));
            } catch (Throwable t) {
                out.add("FAIL " + NAMES.get(i) + ": " + t);
            }
        }
        return out;
    }

    public static void clickMergesBeyond64(Setup s) {
        s.chest.setStack(0, new ItemStack(Items.DIRT, 64));
        s.handler.setCursorStack(new ItemStack(Items.DIRT, 64));
        s.handler.onSlotClick(0, 0, SlotActionType.PICKUP, s.player);
        expect("chest slot after left-click merge", s.chest.getStack(0).getCount(), 128);
        expect("cursor after merge", s.handler.getCursorStack().getCount(), 0);

        s.handler.setCursorStack(new ItemStack(Items.DIRT, 64));
        s.handler.onSlotClick(0, 1, SlotActionType.PICKUP, s.player);
        expect("chest slot after right-click", s.chest.getStack(0).getCount(), 129);
        expect("cursor after right-click", s.handler.getCursorStack().getCount(), 63);
    }

    public static void shiftClickIntoChestMerges(Setup s) {
        s.chest.setStack(0, new ItemStack(Items.DIRT, 64));
        s.player.getInventory().setStack(0, new ItemStack(Items.DIRT, 64));
        s.player.getInventory().setStack(1, new ItemStack(Items.DIRT, 10));
        int hotbar0 = PLAYER_FIRST_SLOT + 27; // main inventory 9..35 is added first, hotbar 0..8 last
        s.handler.onSlotClick(hotbar0, 0, SlotActionType.QUICK_MOVE, s.player);
        s.handler.onSlotClick(hotbar0 + 1, 0, SlotActionType.QUICK_MOVE, s.player);
        expect("chest slot after two shift-clicks", s.chest.getStack(0).getCount(), 138);
        expect("chest slot 1 stays empty", s.chest.getStack(1).getCount(), 0);
        expect("player hotbar 0 emptied", s.player.getInventory().getStack(0).getCount(), 0);
    }

    public static void takingOutGivesNormalStacks(Setup s) {
        s.chest.setStack(0, new ItemStack(Items.DIRT, 500));
        s.handler.onSlotClick(0, 0, SlotActionType.PICKUP, s.player);
        expect("cursor after picking up from 500", s.handler.getCursorStack().getCount(), 64);
        expect("chest after pickup", s.chest.getStack(0).getCount(), 436);
        s.handler.setCursorStack(ItemStack.EMPTY);

        s.handler.onSlotClick(0, 0, SlotActionType.QUICK_MOVE, s.player);
        expect("chest after shift-click out", s.chest.getStack(0).getCount(), 0);
        // 436 left: insertItem(fromLast=true) fills hotbar 8 downwards, 6 x 64 then the remaining 52 in hotbar 2
        expect("player hotbar 8", s.player.getInventory().getStack(8).getCount(), 64);
        expect("player hotbar 3", s.player.getInventory().getStack(3).getCount(), 64);
        expect("player hotbar 2", s.player.getInventory().getStack(2).getCount(), 52);
        expect("player hotbar 1 untouched", s.player.getInventory().getStack(1).getCount(), 0);
    }

    public static void identicalSwordsStack(Setup s) {
        ItemStack a = new ItemStack(Items.DIAMOND_SWORD); a.setDamage(10);
        ItemStack b = new ItemStack(Items.DIAMOND_SWORD); b.setDamage(10);
        ItemStack c = new ItemStack(Items.DIAMOND_SWORD); c.setDamage(11);
        s.chest.setStack(0, a);
        s.handler.setCursorStack(b);
        s.handler.onSlotClick(0, 0, SlotActionType.PICKUP, s.player);
        expect("identical swords merged", s.chest.getStack(0).getCount(), 2);
        expect("cursor empty", s.handler.getCursorStack().getCount(), 0);

        s.handler.setCursorStack(c);
        s.handler.onSlotClick(0, 0, SlotActionType.PICKUP, s.player);
        expect("different sword not merged", s.chest.getStack(0).getCount(), 2);
        expect("swap of oversized stack blocked, cursor keeps sword", s.handler.getCursorStack().getCount(), 1);

        s.handler.setCursorStack(ItemStack.EMPTY);
        s.handler.onSlotClick(0, 0, SlotActionType.PICKUP, s.player);
        expect("one sword picked up", s.handler.getCursorStack().getCount(), 1);
        expect("one sword left", s.chest.getStack(0).getCount(), 1);
    }
}
