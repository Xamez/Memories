package fr.xamez.memories.runnables;

import fr.xamez.memories.struct.AbstractStruct;
import fr.xamez.memories.struct.Arena;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class HitBoxRunnable extends BukkitRunnable {

    public static final ArrayList<AbstractStruct> HIT_BOX = new ArrayList<>();
    public static final float PARTICLE_DISTANCE = 0.25f;

    @Override
    public void run() {
        for (AbstractStruct struct : HIT_BOX) {
            if (struct.getFirstPoint() != AbstractStruct.DEFAULT_LOCATION && struct.getSecondPoint() != AbstractStruct.DEFAULT_LOCATION) {
                struct.showHitBox(struct instanceof Arena ? Color.fromRGB(34, 242, 103) : Color.fromRGB(245, 79, 51));
            }
            if (struct instanceof final Arena arena) {
                final Location loc = arena.getSpawnPoint();
                if (loc != AbstractStruct.DEFAULT_LOCATION)
                    for (double y = 0d; y < 2d; y += PARTICLE_DISTANCE) {
                        loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(0, y, 0), 1,
                                new Particle.DustOptions(Color.fromRGB(30, 155, 252), 1));
                    }
            }
        }
    }
}
