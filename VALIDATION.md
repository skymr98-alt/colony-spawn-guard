# Validation — 2026-10-22

## Build

- JDK 17.0.20.1, Gradle 8.1.1, ForgeGradle 6.0.36.
- Forge 1.20.1-47.2.0 baseline (the target user profile uses Forge 47.4.10).
- Compiled and reobfuscated against the exact MineColonies 1.20.1-1.1.1301 JAR.
- Dependency SHA-256: `8538f46fb32abd638767a021240e4c9a5a1413a820b01caec1113fea2eeb9196`.

## Server integration

Forge GameTest server started with these exact dependencies from the target profile:

- MineColonies 1.20.1-1.1.1301
- Structurize 1.20.1-1.0.818
- BlockUI 1.20.1-1.0.193
- Domum Ornamentum 1.20.1-1.0.296

The integration test creates a real MineColonies colony and posts real Forge spawn events through the event bus. It checks:

1. Natural zombie PositionCheck outside a colony is unchanged.
2. FinalizeSpawn outside a colony is not cancelled.
3. Natural zombie PositionCheck inside a claimed colony is denied.
4. FinalizeSpawn inside the colony marks the spawn cancelled.
5. An empty spawn-egg blacklist leaves PositionCheck unchanged.
6. A non-blacklisted pig remains allowed inside the colony.

Result: **1 required GameTest passed**, containing all six assertions above, followed by a normal server shutdown.

Reproduce with local dependency JARs in a directory (use the exact names/versions listed above):

```powershell
.\gradlew.bat runGameTestServer '-Pminecolonies_jar=C:\mods\minecolonies-1.20.1-1.1.1301.jar' '-Pintegration_mods=C:\mods'
```

The test module and empty test template are included in the source archive; they are not included in the release JAR. Only the test working directory is used.

## Scope

This checks the mod lifecycle, configuration loading, the exact MineColonies API, territory claims, and Forge event handling on a headless test server. It does not constitute a full playthrough of the user's modpack. Client joining, every mod's custom spawn path, and MineColonies raids have not been tested. See README for limitations.
