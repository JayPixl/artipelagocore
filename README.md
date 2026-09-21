This is a collection of QoL and management mods for the upcoming Artipelago Cobblemon server. You are free to download but it is tailored for our specific use cases. Stay tuned for the server launch coming soon!

# Features
- Region Market module bridging YAWP and Lightman's Currency (`/regionmarket`, `/atm`)
- Events module with JSON presets, weekly/one-time schedules, and typed gameplay effects (`/events`)
- YAWP compat fixes (visual sync, hoe-till, fluid placement guard)
- Custom crafting ingredients (raw copper/iron/gold chunks, lapis/emerald/diamond shards)
- PokeCapsule occupied-pokeball placement compat

See `AGENTS.md` for the module map and agent workflow, and `CONTEXT.md` for canonical terms.

## Region Market

YAWP regions listed for sale or starter claim, paid via Lightman's Currency bank accounts. Listings persist per level in world saved data.

```text
/regionmarket add <regionId> <cost>
/regionmarket add <regionId> starter
/regionmarket remove <regionId>
/regionmarket list
/regionmarket buy <regionId>
/regionmarket info
/regionmarket claimstarterhousing
/atm
```

`add`, `remove`, and `list` require permission level 2. Starter listings cost nothing and are claimed once per player via `claimstarterhousing`. Non-starter listings transfer YAWP ownership on purchase.

## Events

The events module writes its global and recurring-event configuration to
`config/artipelago/events.json` on first server start. Scheduled events are persisted in the
world's saved data, so they survive restarts.

Operator commands start configured presets and manage the scheduler:

```text
/events list
/events list detail
/events start <preset> <minutes>
/events stop <preset>
/events shutdown
/events reload
```

Presets, one-time schedules, and weekly UTC schedules are configured in `events.json`; one-time
schedule timestamps use `YYYY-DD-MM@HH:MM` (for example, `2026-26-08@15:00`). For
example, `/events start weekend 120` starts the `weekend` preset for two hours. Integrations can obtain active effects through
`EventManager.getActiveEffects` or stack a typed multiplier with `EventManager.getMultiplier`.
Effects are stored as a type plus arbitrary JSON parameters, allowing granular effects such as
a species-specific spawn chance and hidden-ability flag without changing the event format.

Effect schema source of truth: `schemas.txt` plus `src/main/java/io/jaypixl/artipelagocore/events/effect/EventEffect.java`.
