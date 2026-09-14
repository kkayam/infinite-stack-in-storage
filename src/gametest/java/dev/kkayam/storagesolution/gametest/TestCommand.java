package dev.kkayam.storagesolution.gametest;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/** /sstest [block id] runs the click tests on a live server against the given container block (default chest). */
public class TestCommand implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("sstest").requires(s -> s.hasPermissionLevel(2))
                        .executes(ctx -> run(ctx.getSource(), Blocks.CHEST))
                        .then(CommandManager.argument("block", StringArgumentType.greedyString())
                                .executes(ctx -> run(ctx.getSource(), Registries.BLOCK.get(new Identifier(StringArgumentType.getString(ctx, "block"))))))));
    }

    private static int run(ServerCommandSource src, Block block) {
        ClickTests.Setup probe = ClickTests.setupBlockOnly(src.getWorld(), new BlockPos(0, 10, 0), block);
        String header = "Testing " + Registries.BLOCK.getId(block) + " maxCountPerStack=" + (probe == null ? "n/a" : probe.chest().getMaxCountPerStack());
        src.sendFeedback(() -> Text.literal(header), false);
        System.out.println("[SSTEST] " + header);
        for (String line : ClickTests.runAll(src.getWorld(), new BlockPos(0, 10, 0), block)) {
            System.out.println("[SSTEST] " + line);
            src.sendFeedback(() -> Text.literal(line), false);
        }
        return 1;
    }
}
