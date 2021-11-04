package fr.xamez.memories.struct;

import fr.xamez.memories.Memories;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.ArrayList;


public class Structure extends AbstractStruct implements Cloneable {

    public static ArrayList<Structure> STRUCTURES = new ArrayList<>();

    public static ArrayList<Integer> FALLING_BLOCKS_STRUCTURE = new ArrayList<>();

    public Structure(String name) {
        super(name);
    }

    /**
     * The structure will be paste based on the default f3 axes of minecraft
     * Falling blocks will be paste instead of normal block to avoid cheating
     * @param location The location where the structure will be paste
     */
    public void spawnStructure(Player p, Location location) {

        final ArrayList<Block> structureBlocks = new ArrayList<>();

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

        final PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().b;

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            final int newX = baseX + (x - bottomBlockX);
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                final int newZ = baseZ + (z - bottomBlockZ);
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    final int newY = baseY + (y - bottomBlockY);

                    final Block originBlock = world.getBlockAt(x, y, z);
                    final Location newLocation = new Location(world, newX, newY, newZ);

                    /*final FallingBlock fallingBlock = world.spawnFallingBlock(newLocation.getBlock().getLocation().add(0.5, 0, 0.5), originBlock.getBlockData());
                    fallingBlock.setGravity(false);
                    fallingBlock.setDropItem(false);*/

                    // TODO : fix this
                    final CraftWorld craftWorld = (CraftWorld) world;
                    final IBlockData blockData = craftWorld.getHandle().getType(new BlockPosition(x, y, z));
                    final EntityFallingBlock fallingBlock = new EntityFallingBlock(craftWorld.getHandle(), newLocation.getBlock().getX() + 0.5, newLocation.getBlock().getY(), newLocation.getBlock().getZ() + 0.5, blockData);
                    fallingBlock.setLocation(newLocation.getX() + 0.5, newLocation.getY(), newLocation.getZ() + 0.5, 0, 0);
                    fallingBlock.setNoGravity(true);
                    fallingBlock.setInvulnerable(true);
                    final PacketPlayOutSpawnEntity fallingBlockPacket = new PacketPlayOutSpawnEntity(fallingBlock);
                    playerConnection.sendPacket(fallingBlockPacket);

                    structureBlocks.add(originBlock);
                    FALLING_BLOCKS_STRUCTURE.add(fallingBlock.getId());
                }
            }
        }
        setBlockList(structureBlocks);
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
