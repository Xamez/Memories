package fr.xamez.memories.utils;

import org.bukkit.Location;

public class Config {

    private Location spawnLocation;
    private double worldBoarderRadius;

    public Config(Location location, double radius) {
        this.spawnLocation = location;
        this.worldBoarderRadius = radius;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public double getWorldBoarderRadius() {
        return worldBoarderRadius;
    }

    public void setWorldBoarderRadius(double worldBoarderRadius) {
        this.worldBoarderRadius = worldBoarderRadius;
    }

}
