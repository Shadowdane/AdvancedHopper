# Advanced Hopper

Advanced Hopper adds an upgradable hopper to Minecraft 26.2 on Fabric. It functions just like a regular Minecraft hopper with all pull/push/redstone mechanics. It has a 4-tick transfer cooldown and a dedicated slot for one Stack Upgrade.

Without an upgrade, the hopper moves one item per pull or push. Install a Stack Upgrade to move more items at a time between inventories.

## Modrinth

A link will be added here once Advanced Hopper is published on Modrinth.

## Items

| Item | Use |
| --- | --- |
| Advanced Hopper | Upgradable Hopper that transfers items and holds one Stack Upgrade. |
| Upgrade Base | Crafting component for Stack Upgrade (I) and smithing template for Stack Upgrade (V). |
| Stack Upgrade (I) | Copper tier. Moves up to 4 items per pull or push by default. |
| Stack Upgrade (II) | Iron tier. Moves up to 8 items per pull or push by default. |
| Stack Upgrade (III) | Gold tier. Moves up to 16 items per pull or push by default. |
| Stack Upgrade (IV) | Diamond tier. Moves up to 32 items per pull or push by default. |
| Stack Upgrade (V) | Netherite tier. Moves up to 64 items per pull or push by default. |

## Dependencies

| Requirement | Version |
| --- | --- |
| Minecraft Java Edition | v26.2 |
| Java | v25 or newer |
| Fabric Loader | v0.20.1 or newer |
| Fabric API | v0.161.0+26.2 or newer for Minecraft v26.2 |
| Mod Menu | Optional. Adds an in-game configuration screen. |
| Jade | Optional. Adds on screen tooltip support. |

For multiplayer, install Advanced Hopper and Fabric API on both the server and each client.

## Configuration

Set each tier's transfer limit in `config/advanced_hopper.toml` or through Mod Menu in single-player. Each limit can be set from 1 to 64. A limit of 1 gives that tier the same transfer size as an unupgraded Advanced Hopper.

Changes saved through Mod Menu apply immediately in single-player. Restart Minecraft after editing the TOML file manually. On a dedicated server, edit the server's config and restart the server. Clients receive the server's active limits when they join.
