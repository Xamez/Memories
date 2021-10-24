package fr.xamez.memories.arena;

import fr.xamez.memories.Memories;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.lighting.LightEngine;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class Arena extends AbstractStruct implements Cloneable {

    public static ArrayList<Arena> ARENAS = new ArrayList<>();

    private Location spawnPoint;

    public Arena(String name){
        super(name);
        this.spawnPoint = DEFAULT_LOCATION;
    }

    public void destroyEffect(Player p){
        final World world = firstPoint.getWorld();
        world.playSound(p.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        final WorldServer worldServer = ((CraftWorld) world).getHandle();

        int topBlockX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
        int bottomBlockX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());

        int topBlockY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
        int bottomBlockY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

        int topBlockZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
        int bottomBlockZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());

        final LightEngine lightEngine = worldServer.getChunkProvider().getLightEngine();
        final Set<Chunk> chunks = new HashSet<>();

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                final Chunk nmsChunk = worldServer.getChunkAt(x >> 4, z >> 4);
                for (int y = bottomBlockY; y <= topBlockY; y++) {

                    final Block block = world.getBlockAt(x, y, z);
                    if (block.isEmpty()) continue;

                    // if double slab, give one more
                    if (block.getBlockData() instanceof Slab)
                        if (((Slab) block.getBlockData()).getType().equals(Slab.Type.DOUBLE)) // if the block is a double slab, we give 1 more
                            p.getInventory().addItem(new ItemStack(block.getType()));

                    // get section or create one
                    ChunkSection cs = nmsChunk.getSections()[block.getY() >> 4];
                    if (cs == null) {
                        cs = new ChunkSection(block.getY() >> 4);
                        nmsChunk.getSections()[block.getY() >> 4] = cs;
                    }

                    // effects & set block
                    p.getInventory().addItem(new ItemStack(block.getType()));
                    p.spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation(),5);
                    cs.setType(block.getX() & 15, block.getY() & 15, block.getZ() & 15, Blocks.AIR.getBlockData());

                    // update block for player
                    chunks.add(nmsChunk);
                    lightEngine.a(new BlockPosition(x, y ,z));
                }
            }
        }
        for (Chunk chunk : chunks){
            final PacketPlayOutUnloadChunk packetPlayOutUnloadChunk = new PacketPlayOutUnloadChunk(chunk.getPos().getRegionX(), chunk.getPos().getRegionZ());
            final PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk);
            final PacketPlayOutLightUpdate packetPlayOutLightUpdate = new PacketPlayOutLightUpdate(chunk.getPos(), lightEngine, null, null, true);
            final EntityPlayer player = ((CraftPlayer) p).getHandle();
            player.b.sendPacket(packetPlayOutUnloadChunk);
            player.b.sendPacket(packetPlayOutMapChunk);
            player.b.sendPacket(packetPlayOutLightUpdate);
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
