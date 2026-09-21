# Spec: Version Cleanup (consolidate 0.3.1–0.3.3 → 0.3.1)

Status: ready-for-agent

## Problem Statement

Local `master` is 12 commits ahead of `origin/master` (unpushed) and the version was bumped three times in one day (`0.3.0` → `0.3.1` → `0.3.2` → `0.3.3`) for docs/workflow-only changes. The operator sees version inflation with no gameplay change, and the maintainer cannot tell what the first pushed public version should be. Directly editing the version string backwards (`0.3.3` → `0.3.1`) would leave confusing history and look like a downgrade.

## Solution

Before the first push, consolidate the unpushed work into a single meaningful version (`0.3.1`) on a proper branch off `master`, with one player/operator-facing changelog entry. Keep tag-gated CurseForge behavior unchanged. No published artifact is reverted because nothing was pushed or tagged.

## User Stories

1. As a server operator, I want one version number covering the current unpushed work, so that I know what I am running.
2. As a server operator, I want the changelog to list only player/operator-facing changes, so that I can decide whether to update.
3. As a mod maintainer, I want no backwards version edit (`0.3.3` → `0.3.1`) on top of history, so that Gradle and CurseForge never see a downgrade.
4. As a mod maintainer, I want the 12 local `master` commits moved onto a proper branch, so that trunk-based workflow (`Never commit to master`) is restored.
5. As a mod maintainer, I want `build.gradle.kts`, `CHANGELOG.md`, and `AGENTS.md` version references consistent, so that the next agent does not re-bump.
6. As a CurseForge consumer, I want the first uploaded tag (`v0.3.1`) to carry correct notes, so that I see what changed since `0.3.0`.
7. As a player, I want no change to Preset, Schedule, ScheduledEvent, Effect, Listing, Starter Listing, or Compat Fix behavior from this cleanup, so that gameplay is unaffected.
8. As a mod maintainer, I want existing scheduler tests to still pass, so that I know the cleanup touched no logic.
9. As a mod maintainer, I want Gate 2 (version bump) and Gate 3 (merge/tag) explicitly confirmed, so that no further bump happens silently.

## Implementation Decisions

- Consolidate to `0.3.1` (not keep `0.3.3`): nothing is pushed/tagged, `origin/master` is still `0.3.0`, and the `0.3.2`/`0.3.3` bumps were docs/process-only on the same day. One forward state is cleaner than a backwards edit.
- Touch only version metadata: version property in the build script, root changelog, and the stale version mention in the agent workflow doc. No changes to Preset/Schedule/Effect loading, runtime scheduler, commands, Listings, or Compat Fixes.
- Preserve ADR-0003 (tag-gated CurseForge release): plain merges never upload; only a `vX.Y.Z` tag builds and publishes. This spec creates no tag; tagging `v0.3.1` is a separate Gate 3 step after user testing.
- History fix is local-only: since nothing was pushed, move/rebase the 12 unpushed `master` commits onto `docs/version-cleanup` (or equivalent), then reset local `master` to `origin/master` before merging back. Do not use a revert commit that leaves `0.3.3` in history.
- Changelog policy per repo rule: player/operator-facing only. Fold the three same-day entries into one `0.3.1` entry (cancelled CobbledARC removal; tag-gated releases; notes included as upload content). Internal process stays in commit messages and `docs/adr/`.
- Proposed test seams (confirm with user): (1) build-script version property seam — highest seam, file-level assertion; (2) changelog content seam — file-level assertion; (3) existing logic seam — `EventScheduler` window + `ScheduledEvent` validation tests as regression guard. No new Java seam. One seam touched for the change itself (metadata), one existing seam reused for guard.

## Testing Decisions

- A good test here checks external behavior/files, not implementation details: version string is exactly `0.3.1`, changelog has one `0.3.1` section and no `0.3.2`/`0.3.3` sections, workflow still triggers only on `v*.*.*`.
- Modules tested: no gameplay module is tested for new behavior. Regression guard is the existing pure-logic suite (scheduler windows, `ScheduledEvent` validation) via `./gradlew test` — prior art: `EventSchedulerWindowTest`, `ScheduledEventTest`.
- Manual checklist (no client logic changed, so smoke only): `./gradlew build` compiles; `git log origin/master..HEAD` shows work on branch, not `master`; `git status` clean except intended files.
- Do not create a real `v0.3.1` tag or publish to CurseForge as part of this spec.

## Out of Scope

- Any change to Preset definitions, Schedule rules, ScheduledEvent runtime, Effect appliers, event announcements, Region Market Listings, or Compat Fixes.
- Creating/pushing the `v0.3.1` tag or uploading to CurseForge.
- Rewriting already-pushed history (there is none beyond `origin/master`).
- New unit tests for versioning; new ADRs unless theaseam review surfaces one.
- Bumping to `0.3.4` or keeping `0.3.3`; this spec locks `0.3.1` as the consolidated number.

## Further Notes

- Current evidence: `build.gradle.kts` is `0.3.3`, `origin/master` is `0.3.0` with no `CHANGELOG.md`, release workflow triggers only on tags (`v*.*.*`). All 12 ahead-commits are local.
- Workflow gates apply: Gate 1 (spec confirmed + branch created), Gate 2 (ask before touching version/changelog), Gate 3 (ask before merge; report auto-test + manual checklist). `AGENTS.md` Done-means rule requires the branch itself to carry doc updates + version bump + changelog entry before merge.
- `AGENTS.md` still cites `0.3.0` as the version location example; update that reference as part of the branch so it does not drift again.

## Comments
