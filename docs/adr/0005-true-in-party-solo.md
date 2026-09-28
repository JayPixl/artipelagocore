# True in-party Solo tracking

Solo progress lives on the actor's personal team while every FTB read
(quest book, gates, sweeps) resolves the effective party team. The two
coincide only when unpartied, so a partied player's Solo submits vanish
into a store the book never reads (confirmed live: Dev2's personal store
held completions missing from the party store). Decision (2026-09-25):
implement true in-party Solo — per-player progress AND display while
partied — instead of enforcing unpartied play. Rejected: warn-and-fall-back
to native team behavior when partied; document-only.

## Status

Done already: submit/kill write reroute, server claim gate+record reroute
(`ClaimStoreMixin`, `ClaimAllStoreMixin`), claim-button display
(`ClaimButtonMixin`), personal snapshot sync (slice 1: `PersonalSnapshotMixin`,
`PersonalTeamChangeMixin`, `PersonalSyncMixin`), quest-reset fan-outs
(slice 2: `QuestResetFanoutMixin` covers Global pool+members and Solo
personals; Global also re-asserts the un-claimed postcondition), core
display reroute (slice 4 core: `TaskButtonDisplayMixin` swaps the button to
the task's display store, `QuestButtonDisplayMixin` swaps the board button
to the quest's display store, `ViewQuestPanelDisplayMixin` resolves each
detail-panel read — per-task completion, reward-blocked, gates, repeatable
cooldown, searchable — to its own store; `resolveClaimStoreClient` now
delegates to the quest display resolver), Global repeatable ordering fix
(`RepeatStateMixin` moved from the TAIL of
`Quest.resetProgressIfRepeatable` — which runs before the count and
cooldown are written — to a HEAD snapshot + TAIL compare-and-propagate
around `TeamData.markRewardAsClaimed`, so members receive post-claim
cooldown/count; unchanged claims propagate nothing).
Known open bug (2026-09-27, client-verified, deferred — see issue 04 note):
Global repeatable quests show no cooldown timer after claiming and rewards
cannot be reclaimed afterwards; two propagation fixes (TAIL-read, then
HEAD-snapshot/TAIL-compare) did not change the symptom, so the cause is
elsewhere (suspects: quest-reset fan-out wiping mid-claim repeat state, or
the pool claim-gate). Solo repeatable is green partied and unpartied.
Everything below is still open. No `CONTEXT.md`
change needed: Solo is already defined as isolated inside a party.

## A. Sync the personal record to its player

Partied clients never receive their personal TeamData: login, team-change
and all incremental fan-outs address the effective team only
(`RequestTeamDataMessage.handle`, `ServerQuestFile.playerChangedTeam`,
`TeamData.getOnlineMembers` fan-outs).

1. `RequestTeamDataMessage.handle` (and the team-change path): after the
   effective-team snapshot, also send `SyncTeamDataMessage(personal)` to
   the player, resolved via `getPlayerTeamForPlayerID`. Never overwrite
   `selfTeamData`.
2. Client: new slot for the personal record (e.g.
   `ClientQuestFile.personalTeamData`, handled in
   `FTBQuestsNetClient.syncTeamData`), keyed in `teamDataMap` by personal
   UUID.
3. Live updates need no change unless testing shows otherwise: personal
   writes already fan out to `PlayerTeam.getOnlineMembers()`, i.e. just
   that player. Verify with the book open while the other client submits.

## B. Reroute display reads (client)

~40 sites read `selfTeamData`/`getClientPlayerData()`: `QuestScreen`
(started/completed gating, tooltips, chapter cycling),
`QuestPanel`/`ChapterPanel` (visibility, tints),
`ViewQuestPanel` (truncation, dependency button, cooldown lock),
`TaskButton` (progress bar, checkmark), `QuestButton` (state, locks,
alerts, pins), `PinnedQuestsTracker`. Extend the existing
`resolveClaimStoreClient` pattern to a general
resolve-display-store(quest/task, self, player) helper and redirect each
site; each read needs its own scope check since panels mix scopes.
Suggested order: TaskButton progress/completion + QuestButton state +
ViewQuestPanel gates first (the "can I see/complete" core), visibility /
pins / tooltips / alerts second. Toasts need no change (personal cascade
notifies exactly its player).

## C. Fix server functional reads (not just display)

- Login sweep (`checkQuestBookOnLogin`) and join sweep
  (`playerChangedTeam` auto-claim) run on the effective store: skip Solo
  quests there or resolve per-quest stores, else Solo rewards auto-claim
  natively onto the party. Submit calls inside the sweeps already reroute
  (player known).
- Join-merge (`mergeData`) and party-create snapshot (`copyData`) copy
  personal Solo progress into the party store, producing a stale visible
  copy that diverges. Exclude Solo-task entries from both (mixin on the
  merge/copy path with per-task scope check). Leave-branch claim copy
  stays native (per-player keys are compatible).
- Dependency/lock reads (`canStartTasks`, `areDependenciesComplete`,
  `isVisible`, `getRelativeProgress`): resolve the personal store at call
  sites that have a player (auto-submit tick, inventory detect, dimension,
  stage, commands, kill flow via the submitter handoff). Inventory any
  player-less callers at implementation time.
- Admin reset of a Solo quest targets the party store while progress lives
  personal — same stuck class as the Global reset bug. `ModifyVariable`
  can only swap one store, so mirror the Global approach: let native run,
  then fan out clearing (progress, flags, claims, cooldowns) to the
  personal stores of all members of the viewed team.

## D. Slices and verification

1. Personal snapshot sync (A) — done. 2. Solo reset fan-out (C, reset item)
— done.
3. Sweep guards + merge exclusions (C). 4. Core display reroute (B) —
done for TaskButton, QuestButton, ViewQuestPanel gates + cooldown; left in
B tier 2: QuestPanel/ChapterPanel visibility + tints, pins, tooltips,
alerts (toasts need no change — personal cascade notifies exactly its
player).
5. Remaining display (B). 6. Live-update gap check (A3).
Router helpers that compute target sets (member enumeration, fan-out
targets) stay pure over injected data so they keep unit tests; every
FTB-runtime path verifies through the dual-client checklist
(`runClient` + `runClient2`, one quest per scope per slice), since no
headless seam reaches those call sites.
