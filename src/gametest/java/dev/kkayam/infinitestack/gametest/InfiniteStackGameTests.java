package dev.kkayam.infinitestack.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/** Dev-environment entry: ./gradlew runGametest. Production servers use the /sstest command instead. */
public class InfiniteStackGameTests {
    private static void run(TestContext ctx, int index) {
        try {
            ClickTests.TESTS.get(index).accept(ClickTests.setup(ctx.getWorld(), ctx.getAbsolutePos(new BlockPos(1, 1, 1))));
        } catch (IllegalStateException e) {
            throw new GameTestException(e.getMessage());
        }
        ctx.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE) public void clickMergesBeyond64(TestContext ctx) { run(ctx, 0); }
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE) public void shiftClickIntoChestMerges(TestContext ctx) { run(ctx, 1); }
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE) public void takingOutGivesNormalStacks(TestContext ctx) { run(ctx, 2); }
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE) public void identicalSwordsStack(TestContext ctx) { run(ctx, 3); }
}
