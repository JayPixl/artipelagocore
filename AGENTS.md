# AGENTS.md

## Project

Artipelago Core (`artipelago`) — NeoForge 1.21.1 mod for the Artipelago Cobblemon server.
Version lives in `build.gradle.kts` (`version = "0.3.1"`). See `CONTEXT.md` for canonical terms.

## Module map

- `events/config/` — `EventConfig`, `EventConfigManager` load `config/artipelago/events.json`
- `events/data/` — `ScheduledEvent`, `EventSavedData` (world saved data, survives restarts)
- `events/runtime/` — `EventScheduler`, `EventManager`, `EventModuleEvents`, effect appliers (`SpawnEffect*`, `PlayerXpEffect*`, `PokemonProgressionEffect*`), `EventAnnouncements`
- `events/command/` — `EventCommands` (`/events list|start|stop|shutdown|reload`)
- `events/effect/` — `EventEffect` (typed JSON bonus, shape varies per type; see `schemas.txt`)
- `regionmarket/` — `RegionMarketCommands` (`/regionmarket add|remove|list|buy|info|claimstarterhousing`, `/atm`), `RegionMarketSavedData`, `RegionMarketEntry`, `RegionMarketEvents`
- `yawp/` + `yawp/mixin/` — compat fixes (visual sync, hoe-till, fluid placement guard)
- `pokecapsuleintegration/mixin/` — occupied-pokeball placement patch
- `item/` — `ModItems` crafting ingredients (raw chunks, shards)
- `ArtipelagoCoreMod.java` — mod entry point, event bus + command registration

Effect schema source of truth: `schemas.txt` + `EventEffect.java`. Date format for `once` schedules: `YYYY-DD-MM@HH:MM` UTC (see `EventScheduler.java`).

## Workflow (trunk-based, enforced)

- Never commit to `master`.
- Before starting work: restate the spec, get user confirmation, then `git checkout -b feat/<slug>` (or `fix/<slug>`, `docs/<slug>`).
- Gate 1 — start: spec confirmed + branch created.
- Gate 2 — version bump: ask before touching `build.gradle.kts` or `CHANGELOG.md`.
- Gate 3 — merge/tag: ask before merge; report auto-test results + manual checklist first.

## Build and verify

- `./gradlew test` — pure logic only (scheduler windows, `ScheduledEvent` validation). No `src/test/` exists yet; add JUnit 5 tests for anything that does not need a Minecraft client.
- `./gradlew build` — full compile. Requires Java 21 + Gradle caches / CurseMaven access.
- Anything needing Cobblemon / server / client (spawns, XP, loot, commands, GUIs, mixins) is manual: prepare a checklist with exact commands (e.g. `/events start weekend 120`, `/regionmarket info`) and expected observations.

## Done means

1. Auto tests run, results reported (pass/fail + command output).
2. Manual client checklist prepared for the user.
3. Before merge, the branch itself must include: any `CONTEXT.md`/`AGENTS.md` updates the spec required, the version bump in `build.gradle.kts`, and the `CHANGELOG.md` entry moved out of `[Unreleased]`. `CHANGELOG.md` entries are player/operator-facing only; internal process goes in commit messages and `docs/adr/`. Only merge after the user confirms testing and reviews those three.
4. Release: tag `vX.Y.Z` to trigger `.github/workflows/release.yml`, which builds the JAR and uploads it to CurseForge. Plain merges without a tag are not uploaded. Required secrets: `CURSEFORGE_TOKEN`, `CURSEFORGE_PROJECT_ID`.

## Cancelled scope

CobbledARC / ARC integration is cancelled. Do not reintroduce `arcintegration` code or docs. The commented ARC lines in `ArtipelagoCoreMod.java` were removed with this change.

## Agent skills

### Issue tracker

Issues live as local markdown files under `.scratch/<feature>/`. See `docs/agents/issue-tracker.md`.

### Domain docs

Single-context: root `CONTEXT.md` plus `docs/adr/`. See `docs/agents/domain.md`.
