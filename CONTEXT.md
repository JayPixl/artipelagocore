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
