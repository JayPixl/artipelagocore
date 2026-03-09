package io.jaypixl.artipelagocore.regionmarket;

public class RegionMarketEntry {

    private final String id;
    private final int cost;
    private final String owner;
    private final boolean isStarter;

    public RegionMarketEntry(String id, int cost, String owner, boolean isStarter) {
        this.id = id;
        this.cost = cost;
        this.owner = owner;
        this.isStarter = isStarter;
    }

    public String getId() { return id; }

    public int getCost() { return cost; }

    public String getOwner() { return owner; }

    public boolean getIsStarter() { return isStarter; }

}