package fr.xamez.memories.game;

import org.bukkit.Location;

public class Config {

    private final Location location;
    private final double radius;

    public Config(Location location, double radius) {
        this.location = location;
        this.radius = radius;
    }

    public Location getLocation() {
        return location;
    }

    public double getRadius() {
        return radius;
    }
}
