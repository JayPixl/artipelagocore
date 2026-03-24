package io.jaypixl.artipelagocore.arcintegration.mixin;

import com.daqem.arc.api.action.result.ActionResult;
import io.jaypixl.artipelagocore.arcintegration.api.ACActionResultAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ActionResult.class)
public class MixinActionResult implements ACActionResultAccess {

    @Unique
    private float artipelagocore$expMultiplier = 1.0f;

    @Unique
    private float artipelagocore$catchRateMultiplier = 1.0f;

    @Unique
    private final Map<String, Float> artipelagocore$spawnBucketMultipliers = new HashMap<>();

    @Unique
    private boolean artipelagocore$hiddenAbility = false;

    @Unique
    private int artipelagocore$evBonusMin = 0;

    @Unique
    private int artipelagocore$evBonusMax = 0;

    @Unique
    private int artipelagocore$ivBonusMin = 0;

    @Unique
    private int artipelagocore$ivBonusMax = 0;

    @Override
    public float artipelagocore$getExpMultiplier() {
        return artipelagocore$expMultiplier;
    }

    @Override
    public void artipelagocore$setExpMultiplier(float multiplier) {
        this.artipelagocore$expMultiplier = multiplier;
    }

    @Override
    public float artipelagocore$getCatchRateMultiplier() {
        return artipelagocore$catchRateMultiplier;
    }

    @Override
    public void artipelagocore$setCatchRateMultiplier(float multiplier) {
        this.artipelagocore$catchRateMultiplier = multiplier;
    }

    @Override
    public Map<String, Float> artipelagocore$getSpawnBucketMultipliers() {
        return artipelagocore$spawnBucketMultipliers;
    }

    @Override
    public void artipelagocore$addSpawnBucketMultiplier(String bucket, float multiplier) {
        artipelagocore$spawnBucketMultipliers.merge(bucket, multiplier, (left, right) -> left * right);
    }

    @Override
    public boolean artipelagocore$getHiddenAbility() {
        return artipelagocore$hiddenAbility;
    }

    @Override
    public void artipelagocore$withHiddenAbility() {
        artipelagocore$hiddenAbility = true;
    }

    @Override
    public int artipelagocore$getEvBonusMin() {
        return artipelagocore$evBonusMin;
    }

    @Override
    public int artipelagocore$getEvBonusMax() {
        return artipelagocore$evBonusMax;
    }

    @Override
    public void artipelagocore$setEvBonusRange(int min, int max) {
        this.artipelagocore$evBonusMin = min;
        this.artipelagocore$evBonusMax = max;
    }

    @Override
    public int artipelagocore$getIvBonusMin() {
        return artipelagocore$ivBonusMin;
    }

    @Override
    public int artipelagocore$getIvBonusMax() {
        return artipelagocore$ivBonusMax;
    }

    @Override
    public void artipelagocore$setIvBonusRange(int min, int max) {
        this.artipelagocore$ivBonusMin = min;
        this.artipelagocore$ivBonusMax = max;
    }

    @Inject(method = "merge", at = @At("TAIL"))
    private void artipelagocore$mergeExpMultiplier(ActionResult other, CallbackInfoReturnable<ActionResult> cir) {
        if (other instanceof ACActionResultAccess access) {
            this.artipelagocore$expMultiplier *= access.artipelagocore$getExpMultiplier();
            this.artipelagocore$catchRateMultiplier *= access.artipelagocore$getCatchRateMultiplier();
            access.artipelagocore$getSpawnBucketMultipliers().forEach(this::artipelagocore$addSpawnBucketMultiplier);
            this.artipelagocore$hiddenAbility |= access.artipelagocore$getHiddenAbility();
            this.artipelagocore$evBonusMin += access.artipelagocore$getEvBonusMin();
            this.artipelagocore$evBonusMax += access.artipelagocore$getEvBonusMax();
            this.artipelagocore$ivBonusMin += access.artipelagocore$getIvBonusMin();
            this.artipelagocore$ivBonusMax += access.artipelagocore$getIvBonusMax();
        }
    }
}
