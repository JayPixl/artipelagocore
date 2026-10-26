# Cobblemon version policy

Decision (2026-10-26): one JAR supports exactly one Cobblemon minor line. The build
targets the newest released patch of that line, and `neoforge.mods.toml` declares a
closed range: lowest verified minor `.0` as the floor, the next minor as the exclusive
ceiling. 0.5.0 therefore ships `cobblemon [1.8.0,1.9)`, built against `1.8.3+1.21.1`.

## Status

Applied in 0.5.0. Replaces the open-ended `[1.7.2,)` range, which was already untrue:
the sources compiled against 1.8 APIs while the manifest promised 1.7.2.

## Why

The mod's Cobblemon coupling is mostly compile-checked (6 files import Cobblemon, no
reflection), so the compiler catches removals. The surface that is *not* compile-checked
is the part that matters:

- `@Mixin(targets = "com.cobblemon.mod.common.item.MedicinalLeekItem")` — a string target
  whose injected method (`use(Level, Player, InteractionHand)`) can change invisibly.
- `SpawnEffectOverlay` mutates `SpawnPool.details`, `SpawnDetail` weights and
  `BestSpawnerConfig.worldBuckets` in memory, and deserialises spawn details through
  `SpawnLoader`'s Gson. Those JSON keys and lifecycle hooks are internal API.
- Third-party addons (Cobblemon Occupied Poké Balls) mixin into Cobblemon and vanilla
  classes themselves, so their compatibility decides ours.

With those couplings, an open upper bound is a promise nobody can keep: loading 0.5.0 on
Cobblemon 1.9 would either work by luck or crash deep in the mixin phase, after the world
is already loading.

## What the 1.8.0 -> 1.8.3 bump cost

Zero source changes were needed: the full tree compiled against 1.8.3 unchanged, the
`MedicinalLeekItem#use` target still exists with the same descriptor, and
`PokeBallItem#use` (targeted by the addon's own mixin) is unchanged. The upgrade work was
the addon swap plus metadata, not API repair.

## Consequences

- A Cobblemon bump is: change the build coordinate, rebuild, re-verify the mixin targets
  with `javap` against the new jar, re-check `SpawnEffectOverlay`'s spawn-detail JSON
  keys, then run the manual client checklist.
- 1.8.x patches are absorbed without a policy decision; 1.9 requires widening the range
  in a release of its own.
- The floor is a claim that must stay honest: only raise it to versions actually built
  and loaded.

## Rejected

- Open-ended `[x.y,infinity)` ranges — the previous state; unverifiable and silently
  broken on every minor.
- Dual-minor support via reflection guards — the mixin targets cannot be guarded cheaply,
  so a "supported" older minor would still crash at load.
- Exact pinning (`[1.8.3]`) — forces a release for every Cobblemon patch even when the
  only change is upstream artwork.
