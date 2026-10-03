# cavecompass

A Spigot plugin that gives you a compass which always points toward the
nearest big, open cave — even underground, even if you can't see it.

It works by scanning outward from the player for natural `CAVE_AIR` blocks
(the block type Minecraft's cave generation actually uses, distinct from
regular open-air), filtering out thin crevices and single-block pockets in
favour of genuinely open caverns, and then pointing the compass at that
coordinate using the same "lodestone tracker" mechanism vanilla lodestone
compasses use. That targeting is purely coordinate-based — no line of sight
involved — so it points correctly at a cave through solid rock.

A compass needle can only ever show a horizontal direction, though — it
can't indicate "up" or "down". So the item's tooltip also shows a live
vertical hint (e.g. "▼ 14 blocks down"), giving full 3D guidance together
with the needle.

Instead of caves, a compass can also be set to look for any block type —
for example a group of bee nests — with its own search radius.

## Usage

```
/cavecompass
```

Gives you a Cave Compass. It updates automatically — needle and vertical
hint both — while you carry it anywhere in your inventory, including the
offhand; no need to re-request it.

```
/cavecompass [cave|<block>] [radius] [count]
```

| Example | Searches for |
|---|---|
| `/cavecompass` | the nearest open cave, default radius (`search-radius`) |
| `/cavecompass cave 96` | the nearest open cave within 96 blocks |
| `/cavecompass bee_nest 64` | a group of at least `default-cluster-size` (3) bee nests within 64 blocks |
| `/cavecompass bee_nest 100 2` | a group of at least 2 bee nests within 100 blocks |
| `/cavecompass diamond_ore 32 1` | the nearest single diamond ore within 32 blocks |

`<block>` is any Minecraft block id (tab completion lists them). A
"group" means at least `count` blocks of that type inside the cube of
half-width `cluster-check-radius` around the target. If you hold a compass
from this plugin in your main hand, the command reconfigures that compass;
otherwise you get a new one. Each compass remembers its own settings, so you
can carry a cave compass and a bee-nest compass at the same time. When
nothing is found within the radius the tooltip says so.

## Configuration (`config.yml`)

| Key | Default | Meaning |
|---|---|---|
| `update-interval-ticks` | `40` | How often (in ticks; 20 = 1s) to recompute the nearest cave and re-point compasses. |
| `search-radius` | `48` | Default search radius, in blocks per axis, around each player (overridable per compass via the command). |
| `max-search-radius` | `128` | Largest radius players may choose with the command. |
| `only-loaded-chunks` | `true` | Skip chunks that aren't already loaded, instead of forcing them to load. |
| `min-open-blocks` | `20` | How "big"/airy a cave needs to be to count — a candidate needs at least this many open blocks around it (see below). Lower this if the compass struggles to find anything within `search-radius`. |
| `openness-check-radius` | `2` | Half-width of the cube checked around each candidate when judging its openness above (2 = 5x5x5, 124 neighbours checked). |
| `cluster-check-radius` | `8` | Block search: half-width of the cube in which `count` matching blocks must lie to form a group. |
| `default-cluster-size` | `3` | Block search: `count` used when the command doesn't give one. |

## Building

```
mvn clean package                  # default: Minecraft/Spigot 1.21.11
mvn clean package -P mc-26.1       # Minecraft/Spigot 26.1
```

Two Maven profiles are available since the plugin's own code has no
version-specific dependencies — only the `spigot-api` version and the
`plugin.yml` `api-version` differ between them (see `pom.xml`). Each
produces a differently-named jar in `target/`. Building the `mc-26.1`
profile needs a JDK new enough to read its classfiles (25+); the plugin's
own compiled output still targets release 21 either way, so both jars run
fine on an older server JVM.

CI builds both profiles and attaches both jars directly to the GitHub
Release for every pushed `v*` tag.

## Tests

```
mvn test
```

The cave-finding geometry, the openness filter, the block-cluster filter,
command argument parsing and the vertical-hint formatting are all covered by plain unit tests with no Bukkit server
needed (`BlockLookup` abstracts the world away for this purpose).
