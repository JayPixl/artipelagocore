# Tag-gated CurseForge releases

Merges to `master` alone never upload. Only a `vX.Y.Z` tag triggers the GitHub Action that builds the JAR and uploads it to CurseForge, so routine commits and docs changes stay internal while explicit tags mark live releases.
