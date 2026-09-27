# Goat Apples

Goats headbutt trees and apples fall out of the leaves.

A small server-side mod for Minecraft 1.21.1, on NeoForge and Fabric. Players don't need it
installed, and vanilla clients can join a server that runs it.

Based on the Minecraft feedback post "Apples falling from trees when goats ram them".

## What it does

- A goat that charges, misses and runs into a tree trunk can shake the tree.
- A goat with nothing to ram sometimes picks a nearby fruit tree and charges it.
- A shaken tree drops a few apples from under its leaves, and once in a while a golden apple.
- A tree rests for a while after it's shaken, and nothing drops while too many items already lie
  on the ground nearby.
- Goats charging a tree they picked keep their horns by default.

Everything is configurable in `config/goatapples-common.toml` (NeoForge) or
`config/goatapples-common.json` (Fabric), and changes are picked up while the game runs.
Operators can run `/goatapples` to see whether baited charges shake trees and which horn mode is
set.

## Download

- Modrinth: https://modrinth.com/mod/goat-apples
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/goat-apples

## Building

Each loader is its own Gradle build and needs Java 21.

```
cd neoforge
./gradlew build
```

```
cd fabric
./gradlew build
```

The jar ends up in `build/libs`.

## License

MIT, see [LICENSE](LICENSE).
