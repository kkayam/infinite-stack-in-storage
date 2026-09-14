# Storage Solution (Fabric 1.20.1)

Identical items stack without limit inside storage containers. Taken out, they stack normally.

* **Storage** = every container built on vanilla's lootable-container base class: chests (incl. double and trapped),
  barrels, shulker boxes, ender chests, and modded chests such as Iron Chests, Supplementaries sacks and safes.
  Hoppers, dispensers and droppers are excluded. Configurable in `config/storagesolution.json`:
  `allLootableContainers`, `included` and `excluded` lists of block entity ids.
  Nether Chested is not touched; it already has its own multiplied-stack system.
* Anything with the same item + NBT merges: dirt, stone, but also tools with identical durability/enchantments.
* Picking up, Q/Ctrl+Q dropping, shift-clicking or hotbar-swapping out of storage gives you a normal stack (64, 16, or 1).
* Hoppers and droppers keep feeding an oversized stack instead of stopping at 64.
* Slot labels above 999 are abbreviated (1.5k, 20k, 1.2M); the tooltip shows the exact count.
* Counts over 127 are saved in an extra `storagesolution:count` NBT tag. If the mod is removed the world still loads; each oversized stack falls back to one normal stack.

Required on both client and server. Requires Fabric API.

## Build

```bash
./gradlew build
```

Output: `build/libs/storage-solution-<version>.jar`. `./gradlew runClient` / `runServer` start a dev instance.

## Tests

`./gradlew runGametest` runs four game tests that drive the real `ScreenHandler` click logic against a real chest
(merge past 64, shift-click in, taking out gives normal stacks, identical damaged swords merge).

The same checks are packaged as a `/sstest [block id]` command in `storage-solution-<version>-gametest.jar` (built by
`./gradlew remapGametestJar`). Drop that jar next to the mod on a **production** server to verify the mod inside a
real modpack, where dev-only behaviour cannot hide problems. Lesson learned: a mixin method that overrides an
interface method must live in a mixin that `implements` that interface, otherwise it is not remapped to the
obfuscated name and silently does nothing outside the dev environment.

## How it works (src/main/java/dev/kkayam/storagesolution)

| Mixin | Purpose |
|---|---|
| `LootableContainerBlockEntityMixin`, `EnderChestInventoryMixin` | report `getMaxCountPerStack() == Integer.MAX_VALUE` for configured containers; this is the "is storage" marker and disables vanilla's count clamp in `setStack` |
| `ServerPlayerEntityMixin` + `StorageNetworking` | when a screen opens, the server tells the client which slots are infinite so the client's placeholder inventories (`SimpleInventoryMixin`) stop clamping to 64 |
| `SlotMixin` | storage slots accept any count; taking from a storage slot is capped at a normal stack |
| `ScreenHandlerMixin` | reimplements shift-click merging with per-slot limits; blocks the two vanilla paths that would swap an oversized stack out whole |
| `HopperBlockEntityMixin` | hopper transfers merge into oversized storage stacks |
| `ItemStackMixin`, `PacketByteBufMixin` | persist and network stack counts above the 1-byte vanilla limit |
| `client/DrawContextMixin` | abbreviated slot count labels |
