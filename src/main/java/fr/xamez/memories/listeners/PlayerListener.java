package fr.xamez.memories.listeners;

import fr.mrmicky.fastboard.FastBoard;
import fr.xamez.memories.Memories;
import fr.xamez.memories.commands.MemoriesCMD;
import fr.xamez.memories.runnables.ScoreboardRunnable;
import fr.xamez.memories.struct.AbstractStruct;
import fr.xamez.memories.utils.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class PlayerListener implements Listener {

    public static final WeakHashMap<Player, ArrayList<Location>> PLAYERS_BLOCKS = new WeakHashMap<>();

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        Memories.GAME.setupPlayer(e.getPlayer());
        if (!Memories.GAME.canJoin())
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final FastBoard board = ScoreboardRunnable.BOARDS.remove(p.getUniqueId());
        if (board != null) board.delete();
        if (ArenaEditorListener.EDITOR_MODE.containsKey(p.getUniqueId())) {
            final Pair<AbstractStruct, AbstractStruct> pair = ArenaEditorListener.EDITOR_MODE.get(p.getUniqueId());
            MemoriesCMD.exitEditMode(pair.getFirst().getName(), p);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player) e.setCancelled(true);
    }

    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;
        final WorldBorder worldBorder = e.getPlayer().getWorld().getWorldBorder();
        if (!worldBorder.isInside(e.getPlayer().getLocation()))
            e.getPlayer().teleport(Memories.GAME.getConfig().getSpawnLocation());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if (!Memories.GAME.canBuild() && !e.getPlayer().isOp())
            e.setCancelled(true);
        else
            PLAYERS_BLOCKS.get(e.getPlayer()).add(e.getBlock().getLocation());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        if (!e.getPlayer().isOp())
            e.setCancelled(true);
        else
            PLAYERS_BLOCKS.get(e.getPlayer()).remove(e.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeftClick(PlayerInteractEvent e){
        if (e.getPlayer().isOp()) return;
        if (!Memories.GAME.canBuild()) return;
        if (!e.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        if (!PLAYERS_BLOCKS.get(e.getPlayer()).contains(e.getClickedBlock().getLocation())) return;
        final Block block = e.getClickedBlock();
        final ItemStack itemStack = new ItemStack(block.getType());
        if (block.getBlockData() instanceof Slab)
            if (((Slab) block.getBlockData()).getType().equals(Slab.Type.DOUBLE)) // if the block is a double slab, we give 1 more
                e.getPlayer().getInventory().addItem(itemStack);
        e.getPlayer().getInventory().addItem(itemStack);
        block.setType(Material.AIR);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e){
        if (!e.getPlayer().isOp()) e.setCancelled(true);
    }

}
