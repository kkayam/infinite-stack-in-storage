package dev.kkayam.storagesolution;

/** Implemented (via mixin) by SimpleInventory so the client-side placeholder copies of container inventories can be flagged as infinite storage. */
public interface InfiniteInventory {
    void storagesolution$markInfinite();
}
