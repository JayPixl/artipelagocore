This is a collection of QoL and management mods for the upcoming Artipelago Cobblemon server. You are free to download but it is tailored for our specific use cases. Stay tuned for the server launch coming soon! 🚀

# Features
- Region Market module bridging YAWP and Lightman's Currency
- CobbledARC Module giving compatibility between Cobblemon and ARC Lib

## CobbledARC Reference

The CobbledARC module adds custom ARC actions, rewards, and conditions for Cobblemon gameplay hooks. These are intended to let datapacks and powerups react to Pokemon progression, catching, training, and wild spawning without editing Cobblemon directly.

### Actions

- `artipelago:on_gain_pokemon_exp`
  Fires when a player's Pokemon gains EXP. This can be used for rewards that modify EXP gain, with conditions to distinguish battle EXP from EXP candy.

- `artipelago:on_gain_pokemon_evs`
  Fires when a player's Pokemon gains EVs. This supports both battle EV gains and vitamin-based EV gains.

- `artipelago:on_modify_pokemon_catch_rate`
  Fires when a player attempts to catch a Pokemon. This is the main hook for catch rate modifiers and catch-specific conditions.

- `artipelago:on_hyper_train_pokemon_iv`
  Fires before a Pokemon's IV is hyper trained using an IV candy. This can be used to improve the final trained IV value.

- `artipelago:on_choose_pokemon_spawn_bucket`
  Fires when Cobblemon chooses a spawn bucket for a player's nearby wild spawns. This allows rewards to bias bucket weighting toward buckets such as common or rare.

- `artipelago:on_spawn_wild_pokemon`
  Fires when a player-caused wild Pokemon spawn is created. This is currently used for spawn-side rewards such as assigning hidden abilities.

- `artipelago:on_catch_wild_pokemon`
  Triggers when a wild Pokemon is caught.

- `artipelago:on_defeat_pokemon`
  Triggers upon defeating a Pokemon in battle.

### Rewards

- `artipelago:pokemon_exp_multiplier`
  Multiplies the amount of EXP gained by the Pokemon for the current EXP event.

- `artipelago:pokemon_ev_bonus`
  Adds extra EVs on top of the normal gain. Battle EV bonuses are distributed across the defeated Pokemon's EV yield stats.

- `artipelago:pokemon_catch_rate_multiplier`
  Multiplies the catch rate used for the current catch attempt.

- `artipelago:pokemon_hyper_train_iv_bonus`
  Adds an extra IV bonus during hyper training, up to Cobblemon's normal IV cap.

- `artipelago:pokemon_spawn_bucket_multiplier`
  Multiplies the weight of one named spawn bucket during bucket selection. Multiple matching rewards stack multiplicatively.

- `artipelago:pokemon_hidden_ability`
  Marks a spawned wild Pokemon to receive a hidden ability if the reward triggers. ARC's native `chance` field should be used to control how often this happens.

### Conditions

- `artipelago:in_battle`
  Matches only when the current catch attempt occurs in battle.

- `artipelago:battle_context`
  Matches the current battle context for defeating a Pokemon.
  Supported contexts:
  - `pvp`
  - `wild`
  - `npc`

- `artipelago:pokemon_exp_context`
  Matches the source of EXP gain.
  Supported contexts:
  - `battle`
  - `candy`

- `artipelago:pokemon_ev_context`
  Matches the source of EV gain.
  Supported contexts:
  - `battle`
  - `vitamin`

### Example

This powerup example gives a 15% chance for wild spawns to receive a hidden ability near a given player:

```json
{
  "holder": {
    "type": "jobsplus:powerup",
    "id": "jobsplus:trainer/example_spawn_bonus"
  },
  "type": "artipelago:on_spawn_wild_pokemon",
  "rewards": [
    {
      "type": "artipelago:pokemon_hidden_ability"
    }
  ],
  "conditions": [
    {
      "type": "arc:chance",
      "chance": 15.0
    }
  ]
}
```
