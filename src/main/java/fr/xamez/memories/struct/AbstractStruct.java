package fr.xamez.memories.struct;

import fr.xamez.memories.Memories;
import fr.xamez.memories.runnables.HitBoxRunnable;
import fr.xamez.memories.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public abstract class AbstractStruct {

    public static final Location DEFAULT_LOCATION = new Location(Bukkit.getWorld("world"), 0, 0, 0);

    private transient ArrayList<Block> blockList;

    protected final String name;
    protected Location firstPoint;
    protected Location secondPoint;

    public AbstractStruct(String name) {
        this.name = name;
        this.firstPoint = DEFAULT_LOCATION;
        this.secondPoint = DEFAULT_LOCATION;
        this.blockList = new ArrayList<>();
    }

    public abstract void setObjectTo(AbstractStruct struct);

    /**
     * Get all blocks between two points
     * @param firstPoint First point of the structure
     * @param secondPoint Second point of the structure
     * @return A List of blocks
     */
    protected ArrayList<Block> getBlocks(Location firstPoint, Location secondPoint){
        final ArrayList<Block> blocks = new ArrayList<>();

        int topBlockX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
        int bottomBlockX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());

        int topBlockY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
        int bottomBlockY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

        int topBlockZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
        int bottomBlockZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());

        for(int x = bottomBlockX; x <= topBlockX; x++)
            for(int z = bottomBlockZ; z <= topBlockZ; z++)
                for(int y = bottomBlockY; y <= topBlockY; y++)
                    blocks.add(firstPoint.getWorld().getBlockAt(x, y, z));

        return blocks;
    }


    // TODO MAY BE ADD A GLOW EFFECT WITH A SHULKER WHEN BLOCK ARE INVALID

    /**
     * This method is run asynchronously and 'otherStruct' is considered as the structure to copy
     *
     * Rules:
     * <ul>
     * <li>If the two blocks are the same and they are well oriented: +1</li>
     * <li>If the two blocks are the same but misaligned or in a different state: +0.5</li>
     * <li>If the two blocks are not the same: +0</li>
     * <li>If a block is unplaced : -1.5x (with x number of unplaced blocs)</li>
     * </ul>
     * @param otherStructure The other structure that we will compare
     * @return A percentage that defines how many percent the two structures are the same
     */
    public int compare(AbstractStruct otherStructure) {
        final ArrayList<Block> otherBlockList = otherStructure.getBlockList();
        setBlockList(getBlocks(this.firstPoint, this.secondPoint));
        if (this.blockList.size() != otherBlockList.size()) {
            Bukkit.broadcastMessage(Memories.PREFIX + "§cAttention, une structure a mal été défini, contactez un administrateur (taille des structures différentes)");
            return -1;
        }
        float matchPercentage = 0;
        int realBlocks = 0; // it means non-air blocks
        int placedBlocks = 0;
        for (int i = 0; i < this.blockList.size(); i++) {
            final Block block = blockList.get(i);
            final Block otherBlock = otherBlockList.get(i);
            if (!block.isEmpty()) placedBlocks++;
            if (!otherBlock.isEmpty()) {
                realBlocks++;
                if (block.getBlockData().matches(otherBlock.getBlockData())) // same block and well oriented / same state
                    matchPercentage++;
                else if (block.getType().equals(otherBlock.getType())) // same block but misaligned or in a different state
                    matchPercentage += 0.5;
            }
        }
        matchPercentage -= (realBlocks - placedBlocks) * 1.5;
        return (int) Math.max(0, Utils.truncateDecimal(100 * matchPercentage / realBlocks).floatValue());
    }

    public void showHitBox(Color color){
        final World world = firstPoint.getWorld();
        double minX = Math.min(firstPoint.getX(), secondPoint.getX());
        double minY = Math.min(firstPoint.getY(), secondPoint.getY());
        double minZ = Math.min(firstPoint.getZ(), secondPoint.getZ());

        double maxX = Math.max(firstPoint.getX(), secondPoint.getX());
        double maxY = Math.max(firstPoint.getY(), secondPoint.getY());
        double maxZ = Math.max(firstPoint.getZ(), secondPoint.getZ());
        for (double x = minX; x <= maxX; x+= HitBoxRunnable.PARTICLE_DISTANCE)
            for (double y = minY; y <= maxY; y+= HitBoxRunnable.PARTICLE_DISTANCE)
                for (double z = minZ; z <= maxZ; z+= HitBoxRunnable.PARTICLE_DISTANCE) {
                    int components = 0;
                    if (x == minX || x == maxX) components++;
                    if (y == minY || y == maxY) components++;
                    if (z == minZ || z == maxZ) components++;
                    if (components >= 2)
                        world.spawnParticle(Particle.REDSTONE, new Location(world, x+0.5d, y+0.5d, z+0.5d), 1,
                                new Particle.DustOptions(color, 1));
                }
    }

    public void updateBlockList() {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Memories.class), () -> setBlockList(getBlocks(this.firstPoint, this.secondPoint)));
    }

    public long getSize(){
        return blockList.size();
    }

    protected ArrayList<Block> getBlockList() {
        return blockList;
    }

    public void setBlockList(ArrayList<Block> blockList) {
        this.blockList = blockList;
    }

    public String getName() {
        return name;
    }

    public Location getFirstPoint() {
        return firstPoint;
    }

    public void setFirstPoint(Location firstPoint) {
        this.firstPoint = firstPoint;
    }

    public Location getSecondPoint() {
        return secondPoint;
    }

    public void setSecondPoint(Location secondPoint) {
        this.secondPoint = secondPoint;
    }
}
