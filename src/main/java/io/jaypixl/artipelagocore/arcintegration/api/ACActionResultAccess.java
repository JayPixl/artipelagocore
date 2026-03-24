package io.jaypixl.artipelagocore.arcintegration.api;

import java.util.Map;

public interface ACActionResultAccess {
    float artipelagocore$getExpMultiplier();
    void artipelagocore$setExpMultiplier(float multiplier);

    float artipelagocore$getCatchRateMultiplier();
    void artipelagocore$setCatchRateMultiplier(float multiplier);

    Map<String, Float> artipelagocore$getSpawnBucketMultipliers();
    void artipelagocore$addSpawnBucketMultiplier(String bucket, float multiplier);

    boolean artipelagocore$getHiddenAbility();
    void artipelagocore$withHiddenAbility();

    int artipelagocore$getEvBonusMin();
    int artipelagocore$getEvBonusMax();
    void artipelagocore$setEvBonusRange(int min, int max);

    int artipelagocore$getIvBonusMin();
    int artipelagocore$getIvBonusMax();
    void artipelagocore$setIvBonusRange(int min, int max);
}
