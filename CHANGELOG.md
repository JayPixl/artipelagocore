# Changelog

## [0.6.0] - 2026-11-12

- Added five FTB Quests task types for the Artipelago quest book:
  - **Fish Up Item** — count catches of a specific item or item tag, optionally only in a biome or biome tag. Vanilla fishing and Tide 2 fishing both count.
  - **Break Block** — count blocks broken by hand or tool, filtered by block id or block tag. Creative, fake-player and cancelled breaks do not count.
  - **Customer Transaction** — reward customers for buying from or selling to player shops, counted in items or coins, with optional sold/bought item filters and a server-owned shop toggle.
  - **Merchant Transaction** — credit a shop owner when customers trade at any of their shops, including while the owner is offline.
  - **Trade Item** — count items given or received through `/lctrade` for both participants.
- All five task types respect Tracking Scope (Solo, Team and Global), including Global pooling and offline owner credit.
- Shop tasks ignore money-only trades, self-trades and non-item traders such as gacha machines, slot machines, paygates and command traders.
- A blank item, block or tag filter counts any item or block; a malformed non-blank value never matches.
- Added Lightman's Currency (1.21-2.3.0.5 or newer) and Tide 2 (2.1.x, 2.1.1 or newer) as required dependencies.

## [0.5.0] - 2026-10-26

- Updated for Cobblemon 1.8.3. The mod now requires Cobblemon 1.8.x and no longer loads on 1.7.x.
- Replaced the PokeCapsule occupied-Poké Ball port with the official Cobblemon Occupied Poké Balls mod. Occupied balls still cannot be placed in regions where block placement is denied, and unrelated right-clicks are unaffected.
- Updated to NeoForge 21.1.248 and KotlinForForge 5.12.0.

## [0.4.0] - 2026-09-28

- Added Pasture Teaching: pasture a Cobblemon holding a Mirror Herb alongside one that knows the move in its selected moves to teach one Egg Move into learned moves after 5–15 minutes (tunable via `eggmoves.json`; `/eggmoves reload` applies changes).

## [0.3.1] - 2026-09-21

- Removed the cancelled CobbledARC feature and its documentation.
- Releases are now published to CurseForge only from `vX.Y.Z` tags; plain updates are not uploaded.
- CurseForge releases now include these release notes.
- Added Tracking Scope for FTB Quests: set quests and tasks to Solo (each player progresses on their own, even inside a party), Team (normal party-shared behavior), or Global (the whole server contributes to one shared goal) from a Tracking dropdown in the quest editor. Tasks can override their quest's scope.
- Global quests fan completion out to every party with shared progress toasts, and quest resets and reward claims now clear correctly across all parties.
- Known issue: repeatable Global quests do not show their cooldown timer and cannot be reclaimed after resetting; non-repeatable quests in all scopes are unaffected.
