package fr.xamez.memories.struct;

import fr.xamez.memories.Memories;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.ArrayList;


public class Structure extends AbstractStruct implements Cloneable {

    public static ArrayList<Structure> STRUCTURES = new ArrayList<>();

    public static ArrayList<FallingBlock> FALLING_BLOCKS_STRUCTURE = new ArrayList<>();

    public Structure(String name) {
        super(name);
    }

    /**
     * The structure will be paste based on the default f3 axes of minecraft
     * Falling blocks will be paste instead of normal block to avoid cheating
     * @param location The location where the structure will be paste
     */
    public void spawnStructure(Location location) {

        int topBlockX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
        int bottomBlockX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());

        int topBlockY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
        int bottomBlockY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

        int topBlockZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
        int bottomBlockZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());

        final World world = location.getWorld();
        final int baseX = location.getBlockX();
        final int baseY = location.getBlockY();
        final int baseZ = location.getBlockZ();

        for (int y = bottomBlockY; y <= topBlockY; y++) {
            final int newY = baseY + (y - bottomBlockY);
            for (int x = bottomBlockX; x <= topBlockX; x++) {
                final int newX = baseX + (x - bottomBlockX);
                for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                    final int newZ = baseZ + (z - bottomBlockZ);

                    final Block originBlock = world.getBlockAt(x, y, z);
                    if (originBlock.isEmpty()) continue;

                    final Location newLocation = new Location(world, newX, newY - 20, newZ);
                    final FallingBlock fallingBlock = world.spawnFallingBlock(newLocation.getBlock().getLocation().add(0.5, 0, 0.5), originBlock.getBlockData());
                    fallingBlock.setGravity(false);
                    fallingBlock.setDropItem(false);
                    fallingBlock.setVelocity(new Vector(0, 0, 0));

                    FALLING_BLOCKS_STRUCTURE.add(fallingBlock);
                }
            }
        }
    }

    @Override
    public Structure clone() {
        try {
            return (Structure) super.clone();
        } catch (CloneNotSupportedException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de cloner cette structure !"); // in theory there will never be an error
        }
        return this;
    }

    @Override
    public void setObjectTo(AbstractStruct struct) {
        final Structure structure = (Structure) struct;
        this.setFirstPoint(structure.getFirstPoint());
        this.setSecondPoint(structure.getSecondPoint());
    }

}
