# Changelog

## [Unreleased]

## [0.3.1] - 2026-09-21

- Removed cancelled CobbledARC / ARC integration docs and dead commented code.
- Added `CONTEXT.md`, `AGENTS.md`, and `docs/adr/` for AI-assisted development.
- Added JUnit coverage for `ScheduledEvent` validation and `EventScheduler` time windows (`src/test/`).
- Added tag-gated release workflow (`.github/workflows/release.yml`); requires `CURSEFORGE_TOKEN` + `CURSEFORGE_PROJECT_ID` secrets.
- Tightened `AGENTS.md` release ordering: CONTEXT/AGENTS updates, version bump, and changelog entry must land on-branch before merge.

## [0.3.0] - 2026-09-21

- Current version in `build.gradle.kts`.
