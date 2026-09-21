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

### Compat

**Compat Fix**:
A small patch keeping YAWP or PokeCapsule behavior correct alongside this mod.
_Avoid_: Hack, Workaround, Patch
