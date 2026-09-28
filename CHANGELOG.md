# Changelog

## [0.4.0] - 2026-09-28

- Added Pasture Teaching: pasture a Cobblemon holding a Mirror Herb alongside one that knows the move in its selected moves to teach one Egg Move into learned moves after 5–15 minutes (tunable via `eggmoves.json`; `/eggmoves reload` applies changes).

## [0.3.1] - 2026-09-21

- Removed the cancelled CobbledARC feature and its documentation.
- Releases are now published to CurseForge only from `vX.Y.Z` tags; plain updates are not uploaded.
- CurseForge releases now include these release notes.
- Added Tracking Scope for FTB Quests: set quests and tasks to Solo (each player progresses on their own, even inside a party), Team (normal party-shared behavior), or Global (the whole server contributes to one shared goal) from a Tracking dropdown in the quest editor. Tasks can override their quest's scope.
- Global quests fan completion out to every party with shared progress toasts, and quest resets and reward claims now clear correctly across all parties.
- Known issue: repeatable Global quests do not show their cooldown timer and cannot be reclaimed after resetting; non-repeatable quests in all scopes are unaffected.
