# Quest task types for FTB Quests

FTB Quests 2101.1.36 ships no fishing, block-break or shop-trade task. Its
`StatTask` on `minecraft:fish_caught` counts every catch with no item or biome
filter, `ObservationTask` observes blocks without firing on break, and nothing
in the jar references `ItemFishedEvent`, `BlockEvent.BreakEvent`,
`MerchantOffer` or Lightman's Currency trades. The Artipelago quest book needs
"catch a Cod in an ocean", "break 64 Oak Logs", "sell 64 Oak Logs in your shop"
and "spend 300 coins at shops", so this mod registers five task types:

- `artipelago:fish` — Fish Up Item
- `artipelago:block_break` — Break Block
- `artipelago:customer_transaction` — Customer Transaction
- `artipelago:merchant_transaction` — Merchant Transaction
- `artipelago:trade_item` — Trade Item

Registration happens in the mod constructor on both sides before any quest file
loads, because `TaskTypes.register` is a static map and the client must
construct the same task objects as the server.

## One counter, two filters

Every type stores one `Amount` and one counting mode:

- Fish and Trade Item count item quantities; Break Block counts blocks.
- Shop tasks use `Counts`: `Sold items`, `Bought items`, `Coins received`,
  `Coins paid`. Sold and bought always describe the shop's side (the same
  convention as Lightman's `SALE`/`PURCHASE`); coin labels are relative to the
  credited party and invert between Customer and Merchant.
- Sold and bought filters in a transaction are conjoined qualifiers: a filled
  side must contain a match, even when counting coins. A trade that moved no
  items is discarded before counting, which is the single rule that makes
  money-only trades never count.

Blank filters count any item/block/catch. A non-blank but malformed value never
matches, so a typo fails closed instead of silently counting everything. The
editor uses stock FTB config values; fields that do not apply are dimmed and
unclickable rather than hidden, and keep their value so switching modes back
restores them. Dimming reads the dropdown's pending value because FTB applies
config setters only on Accept.

## Offline owner credit

Customer progress credits the executing buyer. Merchant progress credits the
trader's `PlayerOwner` across all of that owner's traders, including while the
owner is offline. That is the one intentional coupling from `questtasks` into
`questtracking`: `QuestTrackingRouter.resolveStoreForOwner(task, playerId)`
resolves Team from the owner's effective party team, Solo from their personal
team and Global from the pooled store, then writes through
`TeamData.addProgress`. Native login sync and the Global fan-out make the
progress visible later; no queue or custom saved data is needed.

Server-owned and team-owned shops have no `PlayerOwner`, so they never credit an
owner. The Customer `Allow server-owned traders` toggle governs only
server-owned (admin/fake/unowned) shops; player- and team-owned shops always
count for the customer. A trade the executor makes with their own shop never
counts.

## `/lctrade` mixin risk

Lightman's `PlayerTrade` posts no event of any kind. `PlayerTradeMixin` injects
at `RETURN` of the private `tryExecuteTrade()V`, after `completed` is set and
while both item containers are still populated (the completed-trade menu
deliberately skips clearing them). The `@Unique` per-trade guard is mandatory,
not defensive: `tryExecuteTrade` can be re-entered by late confirmation packets
after completion and would otherwise credit the same trade twice. Participant
identity uses `getHostID()`/`getGuestID()` with `UUID.equals`, never
`PlayerTrade.isHost`/`isGuest`, which compare UUIDs by reference. Money is not
counted; only item movement is.

## Rejected alternatives

- Patching FTB's built-in tasks instead of adding types: the filters do not
  exist in any built-in shape.
- A custom config widget or picker screen: stock FTB values plus dimming cover
  every field.
- A per-trader, per-dimension or multi-entry filter: not needed by the quest
  book and adds state that cannot be validated against server data.
- A queue or custom SavedData for offline owner credit: the existing TeamData
  stores plus login sync already provide it.
- A soft-optional Lightman's Currency or Tide integration: both are load-bearing
  on the production server and are declared mandatory.
