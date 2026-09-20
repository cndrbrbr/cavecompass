# cavecompass

A Spigot plugin that gives you a compass which always points toward the
nearest cave — even underground, even if you can't see it.

It works by scanning outward from the player for natural `CAVE_AIR` blocks
(the block type Minecraft's cave generation actually uses, distinct from
regular open-air) and then pointing the compass at that coordinate using the
same "lodestone tracker" mechanism vanilla lodestone compasses use. That
targeting is purely coordinate-based — no line of sight involved — so it
points correctly at a cave through solid rock, exactly as needed.

## Usage

```
/cavecompass
```

Gives you a Cave Compass. It updates automatically while you carry it
(anywhere in your inventory, including the offhand); no need to re-request
it.

## Configuration (`config.yml`)

| Key | Default | Meaning |
|---|---|---|
| `update-interval-ticks` | `40` | How often (in ticks; 20 = 1s) to recompute the nearest cave and re-point compasses. |
| `search-radius` | `48` | How far, in blocks per axis, to search around each player. |
| `only-loaded-chunks` | `true` | Skip chunks that aren't already loaded, instead of forcing them to load. |

## Building

```
mvn clean package
```

Requires Java 25+ (matching the Minecraft/Spigot version this targets,
`26.3-R0.1-SNAPSHOT`). The shaded jar is written to `target/`.
