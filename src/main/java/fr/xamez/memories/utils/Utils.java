package fr.xamez.memories.utils;

import fr.xamez.memories.arena.Arena;
import fr.xamez.memories.arena.Structure;
import org.bukkit.Location;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class Utils {

    public static Optional<Arena> findArenaByName(String name){
        return Arena.ARENAS.stream()
                .filter(arena -> arena.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public static Optional<Structure> findStructureByName(String name){
        return Structure.STRUCTURES.stream()
                .filter(structure -> structure.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public static String getFormattedLocation(Location location){
        return "x: " + truncateDecimal(location.getX()) + ", " +
                "y: " + truncateDecimal(location.getY()) + ", " +
                "z: " + truncateDecimal(location.getZ());
    }

    public static BigDecimal truncateDecimal(double num) {
        return new BigDecimal(String.valueOf(num)).setScale(2, RoundingMode.DOWN);
    }

}
