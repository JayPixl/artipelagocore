# events.json file as the event config source

`EventConfigManager` loads presets, schedules, and effects from `config/artipelago/events.json`, materialized at runtime by `EventScheduler` into `EventSavedData`, because operators need to tune events without rebuilding the JAR. Rejected alternative: hardcoding presets in Java or datapacks.
