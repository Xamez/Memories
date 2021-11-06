package fr.xamez.memories.struct;

import fr.xamez.memories.Memories;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Arena extends AbstractStruct implements Cloneable {

    public static ArrayList<Arena> ARENAS = new ArrayList<>();

    private Location spawnPoint;

    public Arena(String name){
        super(name);
    }

    public void destroyEffect(Player p) {

        for (Block block : Memories.GAME.getCurrentStructure().getBlockList()) {
            if (block.isEmpty()) continue;
            p.getInventory().addItem(new ItemStack(block.getType()));
            if (block.getBlockData() instanceof Slab)
                if (((Slab) block.getBlockData()).getType().equals(Slab.Type.DOUBLE)) // if the block is a double slab, we give 1 more
                    p.getInventory().addItem(new ItemStack(block.getType()));
        }

        final PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().b;

        final PacketPlayOutNamedSoundEffect playerSoundPacket = new PacketPlayOutNamedSoundEffect(SoundEffects.sr, SoundCategory.e, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), 1, 1);
        playerConnection.sendPacket(playerSoundPacket);

        for (FallingBlock fallingBlock : Structure.FALLING_BLOCKS_STRUCTURE)
            fallingBlock.remove();
        Structure.FALLING_BLOCKS_STRUCTURE.clear();
    }

    public void clearArena() {
        int topBlockX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
        int bottomBlockX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());

        int topBlockY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
        int bottomBlockY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

        int topBlockZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
        int bottomBlockZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());

        final World world = getFirstPoint().getWorld();
        final int baseX = getFirstPoint().getBlockX();
        final int baseY = getFirstPoint().getBlockY();
        final int baseZ = getFirstPoint().getBlockZ();

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            final int newX = baseX + (x - bottomBlockX);
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                final int newZ = baseZ + (z - bottomBlockZ);
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    final int newY = baseY + (y - bottomBlockY);
                    if (world.getBlockAt(newX, newY, newZ).isEmpty()) continue;
                    world.getBlockAt(newX, newY, newZ).setType(Material.AIR);
                }
            }
        }
    }

    @Override
    public Arena clone() {
        try {
            return (Arena) super.clone();
        } catch (CloneNotSupportedException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de cloner cette arène !"); // in theory there will never be an error
        }
        return this;
    }

    @Override
    public void setObjectTo(AbstractStruct struct) {
        final Arena arena = (Arena) struct;
        this.setSpawnPoint(arena.getSpawnPoint());
        this.setFirstPoint(arena.getFirstPoint());
        this.setSecondPoint(arena.getSecondPoint());
    }

    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
}
