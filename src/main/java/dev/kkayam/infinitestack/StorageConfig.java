package dev.kkayam.infinitestack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Which block-entity containers stack without limit. Saved to config/infinitestack.json.
 * <p>
 * By default every container that extends vanilla's LootableContainerBlockEntity qualifies, which covers
 * chests, barrels, shulker boxes and most modded chests (Iron Chests, Supplementaries sacks and safes, ...),
 * minus the redstone components in {@code excluded}. Containers built on other base classes can only be
 * added if they never clamp their own stack counts; list them in {@code included}.
 */
public final class StorageConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static StorageConfig instance = new StorageConfig();

    public boolean allLootableContainers = true;
    public List<String> included = new ArrayList<>();
    public List<String> excluded = new ArrayList<>(List.of("minecraft:hopper", "minecraft:dispenser", "minecraft:dropper"));

    private transient Set<Identifier> includedIds = Set.of();
    private transient Set<Identifier> excludedIds = Set.of();

    public static StorageConfig get() {
        return instance;
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(InfiniteStack.MOD_ID + ".json");
        StorageConfig loaded = new StorageConfig();
        try {
            if (Files.exists(path)) {
                loaded = GSON.fromJson(Files.readString(path), StorageConfig.class);
                if (loaded == null) loaded = new StorageConfig();
            }
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(loaded));
        } catch (IOException | RuntimeException e) {
            InfiniteStack.LOGGER.error("Could not read {}; using defaults", path, e);
        }
        loaded.includedIds = parse(loaded.included);
        loaded.excludedIds = parse(loaded.excluded);
        instance = loaded;
    }

    private static Set<Identifier> parse(List<String> ids) {
        Set<Identifier> out = new HashSet<>();
        if (ids != null) for (String s : ids) {
            Identifier id = Identifier.tryParse(s);
            if (id == null) InfiniteStack.LOGGER.warn("Ignoring invalid container id '{}' in config", s);
            else out.add(id);
        }
        return out;
    }

    /** @param lootable whether the block entity extends LootableContainerBlockEntity */
    public boolean isInfinite(BlockEntityType<?> type, boolean lootable) {
        Identifier id = Registries.BLOCK_ENTITY_TYPE.getId(type);
        if (id == null || excludedIds.contains(id)) return false;
        return includedIds.contains(id) || (lootable && allLootableContainers);
    }
}
