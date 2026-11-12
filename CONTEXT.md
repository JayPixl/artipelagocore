# Artipelago Core

QoL and management modules for the Artipelago Cobblemon NeoForge server.

## Language

### Events

**Preset**:
A reusable named template of effects stored in `events.json`.
_Avoid_: Event, Template

**Schedule**:
A weekly or one-time trigger rule linking a Preset to a UTC time window.
_Avoid_: Cron, Timer

**ScheduledEvent**:
A live runtime instance of a Preset with concrete start and end times.
_Avoid_: Event, Active Event

**Effect**:
One typed gameplay bonus inside a Preset, such as `shiny_boost` or `player_xp_boost`.
_Avoid_: Reward, Action, Buff

### Region Market

**Listing**:
A YAWP region offered for purchase or starter claim, with cost and owner state.
_Avoid_: Entry, Plot, Sale

**Starter Listing**:
A Listing claimable for free once per player as starter housing.
_Avoid_: Starter plot, Free region

### Egg Moves

**Egg Move**:
A move a species can receive through ancestry, from its egg pool.
_Avoid_: Tutor move, TM move

**Teacher**:
A pastured Cobblemon that knows a move in its selected moves and acts as the source.
_Avoid_: Parent, Donor

**Student**:
A pastured Cobblemon holding a Mirror Herb that receives into its learned moves.
_Avoid_: Learner, Child

**Pasture Teaching**:
The timed pasture process where a Student learns one Egg Move from a Teacher.
_Avoid_: Breeding, Copying

### Compat

**Compat Fix**:
A small patch keeping YAWP or PokeCapsule behavior correct alongside this mod.
_Avoid_: Hack, Workaround, Patch

### Quest Tracking

**Tracking Scope**:
The rule selecting which progress store a quest or task writes to.
_Avoid_: Mode, Team setting

**Solo**:
A Tracking Scope storing progress on the actor's personal team, isolated even inside a party.
_Avoid_: Player, Individual, Private

**Team**:
A Tracking Scope sharing progress on the effective party team, the FTB native behavior.
_Avoid_: Party, Group, Shared

**Global**:
A Tracking Scope pooling progress on a fixed server team and fanning completion out to all teams.
_Avoid_: Server-wide, Public

### Quest Tasks

**Task Type**:
An FTB Quests task kind registered by Artipelago Core, with its own config, serialization and event detection.
_Avoid_: Quest task, objective

**Item Filter**:
A task filter matching one item id or one item tag. Blank counts any item; a malformed non-blank value never matches.
_Avoid_: Item list, whitelist

**Biome Filter**:
An optional Fish Up Item restriction matching the bobber's biome id or biome tag.
_Avoid_: Biome list, dimension filter

**Customer Transaction**:
A shop task crediting the buyer for items or coins moved at someone else's shop.
_Avoid_: Purchase task, buy task

**Merchant Transaction**:
A shop task crediting the trader's player owner, including while offline, for items or coins moved at their shops.
_Avoid_: Owner task, shop task

**Merchant**:
The player owner of one or more Lightman's Currency traders. A merchant task has no per-trader filter and pools credit across all of that owner's traders.
_Avoid_: Shopkeeper, vendor

**Trade Item**:
A player-to-player `/lctrade` task counting items given, received, or either side.
_Avoid_: Player trade task, barter task
