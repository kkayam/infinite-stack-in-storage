package dev.kkayam.infinitestack;

/** Implemented (via mixin) by SimpleInventory so the client-side placeholder copies of container inventories can be flagged as infinite storage. */
public interface InfiniteInventory {
    void infinitestack$markInfinite();
}
