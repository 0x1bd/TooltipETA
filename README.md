# TooltipETA

TooltipETA is a lightweight client-side Fabric mod that adds practical durability intelligence to item tooltips.

It displays:
- estimated remaining uses for tools and armor (including Unbreaking impact)
- estimated remaining Elytra flight time
- Loyalty trident return ETA and distance in the actionbar
- optional remaining durability and percentage lines
- configurable warning and critical color thresholds

## Features

- Fully client-side
- Supports vanilla and modded damageable items
- Separate toggles for tools, armor, and Elytra
- Two output modes:
  - `DETAILED` (label + value + optional extra lines)
  - `COMPACT` (single concise ETA line)
- In-game config screen via Mod Menu + YACL
- Color-coded Loyalty trident return actionbar with ETA and distance
- Optional per-item whitelist by full item IDs
- Optional extra lines:
  - remaining durability (`x / max`)
  - remaining percent (`yy.y%`)
- Adjustable color thresholds for warning and critical states
- Compact number formatting (`12.5K`, `1.2M`) or full formatting (`12,500`)

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/).
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Install [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin).
4. Install [Mod Menu](https://modrinth.com/mod/modmenu) (recommended for in-game config UI).
5. Drop TooltipETA into your `mods` folder.

## Configuration

TooltipETA uses a config file at:

`config/tooltipeta.json`

The file is generated automatically on first launch.

You can edit settings in-game from:
`Mods -> TooltipETA -> Configure`

## Compatibility

- Minecraft: `26.1.2+` (Fabric)
- Environment: client only

## Development

```bash
./gradlew build
```

Build output:
- `build/libs/TooltipETA-<version>.jar`
