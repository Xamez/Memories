package fr.xamez.memories.arena;

import fr.xamez.memories.Memories;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutLightUpdate;
import net.minecraft.network.protocol.game.PacketPlayOutMapChunk;
import net.minecraft.network.protocol.game.PacketPlayOutUnloadChunk;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkSection;
import net.minecraft.world.level.lighting.LightEngine;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class Structure extends AbstractStruct implements Cloneable {

    public static ArrayList<Structure> STRUCTURES = new ArrayList<>();

    public Structure(String name){
        super(name);
    }

    @Override
    public void setObjectTo(AbstractStruct struct) {
        final Structure structure = (Structure) struct;
        this.setFirstPoint(structure.getFirstPoint());
        this.setSecondPoint(structure.getSecondPoint());
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

    /**
     * The structure will be paste based on the default f3 axes of minecraft
     * @param location The location where the structure will be paste
     */
    public void spawnStructure(Player p, Location location) {
        final World world = firstPoint.getWorld();
        final WorldServer worldServer = ((CraftWorld) world).getHandle();
        int topBlockX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
        int bottomBlockX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());

        int topBlockY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
        int bottomBlockY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

        int topBlockZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
        int bottomBlockZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());

        final int baseX = location.getBlockX();
        final int baseY = location.getBlockY();
        final int baseZ = location.getBlockZ();
        final LightEngine lightEngine = worldServer.getChunkProvider().getLightEngine();
        final Set<Chunk> chunks = new HashSet<>();
        for (int x = bottomBlockX; x <= topBlockX; x++) {
            final int newX = baseX + (x - bottomBlockX);
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                final int newZ = baseZ + (z - bottomBlockZ);
                final Chunk nmsChunk = worldServer.getChunkAt(newX >> 4, newZ >> 4);
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    final int newY = baseY + (y - bottomBlockY);

                    // get section or create one
                    ChunkSection cs = nmsChunk.getSections()[newY >> 4];
                    if (cs == null) {
                        cs = new ChunkSection(newY >> 4);
                        nmsChunk.getSections()[newY >> 4] = cs;
                    }

                    // set block
                    final Block originBlock = world.getBlockAt(x, y, z);
                    final IBlockData iBlockData = ((CraftBlock) originBlock).getNMS();
                    cs.setType(newX & 15, newY & 15, newZ & 15, iBlockData);

                    // update block for player
                    chunks.add(nmsChunk);
                    lightEngine.a(new BlockPosition(newX, newY ,newZ));
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
}
