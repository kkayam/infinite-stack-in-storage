# Storage Solution

**One slot, unlimited items.** Put identical items into a chest and they merge into a single stack, no matter how many. Take them out and they behave exactly like vanilla again.

## What stacks

- Any item with the same id and NBT: 10,000 cobblestone in one slot, 500 oak logs in another.
- Non-stackables too: diamond swords with the **same durability and enchantments** merge. Different durability stays separate, so nothing is ever lost.

## Where it stacks

Chests (single, double, trapped), barrels, shulker boxes, ender chests, and modded containers built on the vanilla chest base class such as **Iron Chests** and **Supplementaries** sacks and safes. Hoppers, dispensers and droppers keep their normal limits so redstone keeps working.

## Taking things out

- Click a stack: you pick up one normal stack (64, 16 or 1).
- Shift-click: moves as much as fits in your inventory.
- Q / Ctrl+Q: drops a normal stack.

## Automation

Hoppers and droppers keep feeding an oversized stack instead of stopping at 64. (With Lithium installed, hopper insertion stops once every slot holds 64+, because Lithium replaces that code.)

## Display

Counts above 999 are shown abbreviated in the slot (1.5k, 20k, 1.2M). Hover for the exact number.

## Config

`config/storagesolution.json`

```json
{
  "allLootableContainers": true,
  "included": [],
  "excluded": ["minecraft:hopper", "minecraft:dispenser", "minecraft:dropper"]
}
```

Add block entity ids to `included` or `excluded` to fine-tune which containers stack without limit.

## Requirements

- Fabric Loader 0.15+, Fabric API
- Required on **both client and server**
- Minecraft 1.20.1

## Safety

Counts above 127 are saved in an extra NBT tag. If you remove the mod, worlds still load and each oversized stack falls back to one normal stack instead of corrupting.

## Links

Source code and issue tracker: https://github.com/kkayam/storage-solution
