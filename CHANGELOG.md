# Changelog

## 1.20.1-1.0.1 — 2026-10-26

- Enable automatic hostile-mob protection across mod namespaces, using Minecraft MONSTER category or Enemy interface.
- Apply the setting to existing configurations without requiring a reset.
- Catch newly created mobs added directly to the world, bypassing normal spawn checks.
- Preserve mobs loaded from disk, peaceful mobs, and known command/spawn-egg reasons unless explicitly blacklisted.
- Add configurable entity/mod exemptions for the automatic rule.
- Add regression tests with registered, unlisted modded entity types and direct world insertion.

## 1.20.1-1.0.0 — 2026-10-22

Initial unofficial Forge 1.20.1 adaptation, based on ColonyWard revision e80d796.

- Build with ForgeGradle 6 and Java 17.
- Target MineColonies 1.20.1-1.1.1301.
- Block configured spawns inside colonies using Forge PositionCheck and FinalizeSpawn.
- Retain upstream spawn-reason lists; remove trial spawners (not in 1.20.1).
- Use a distinct distribution name, mod ID, configuration filename, and project page.
- Preserve GPLv3 and upstream credits; omit the upstream logo.

