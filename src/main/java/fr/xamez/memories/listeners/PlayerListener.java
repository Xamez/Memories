package fr.xamez.memories.listeners;

import fr.mrmicky.fastboard.FastBoard;
import fr.xamez.memories.Memories;
import fr.xamez.memories.game.GameClock;
import fr.xamez.memories.struct.AbstractStruct;
import fr.xamez.memories.commands.MemoriesCMD;
import fr.xamez.memories.runnables.ScoreboardRunnable;
import fr.xamez.memories.utils.Pair;
import org.bukkit.GameMode;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

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
        if (!Memories.GAME.canBuild() && !e.getPlayer().isOp()) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockBreakEvent e){
        if (!Memories.GAME.canBuild() && !e.getPlayer().isOp()) e.setCancelled(true);
    }

}
