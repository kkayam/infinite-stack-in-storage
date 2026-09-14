package dev.kkayam.storagesolution;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StorageSolution implements ModInitializer {
    public static final String MOD_ID = "storagesolution";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        StorageConfig.load();
        LOGGER.info("Storage Solution loaded: storage containers now stack identical items without limit.");
    }
}
