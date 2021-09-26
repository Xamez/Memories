package fr.xamez.memories.listeners;

import fr.xamez.memories.Memories;
import fr.xamez.memories.arena.AbstractStruct;
import fr.xamez.memories.arena.Arena;
import fr.xamez.memories.runnables.HitBoxRunnable;
import fr.xamez.memories.utils.FileUtils;
import fr.xamez.memories.utils.Pair;
import fr.xamez.memories.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class ArenaEditorListener implements Listener {

    public static final HashMap<UUID, Pair<AbstractStruct, AbstractStruct>> EDITOR_MODE = new HashMap<>();

    @EventHandler
    public void playerInteract(PlayerInteractEvent e){
        if (e.getItem() == null) return;
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();
        if (!EDITOR_MODE.containsKey(uuid)) return;
        final Material material = e.getItem().getType();
        final Pair<AbstractStruct, AbstractStruct> pair = EDITOR_MODE.get(uuid);
        if (material.equals(Material.BARRIER) || material.equals(Material.SLIME_BALL)){
            e.setCancelled(true);
            HitBoxRunnable.HIT_BOX.remove(pair.getSecond());
            p.sendMessage(Memories.PREFIX + "§eVous venez de sortir du mode d'édition !");
            p.getInventory().clear();
            EDITOR_MODE.remove(uuid);
            HitBoxRunnable.HIT_BOX.remove(pair.getFirst());
            if (material.equals(Material.BARRIER))
                p.sendMessage(Memories.PREFIX + "§cLes modifications n'ont pas été enregistré");
            else
                p.sendMessage(Memories.PREFIX + "§aLes modifications ont été enregistré");
                pair.getFirst().setObjectTo(pair.getSecond());
                pair.getFirst().updateBlockList();
                FileUtils.saveAllStruct(false);
        } else if (material.equals(Material.STICK)) {
            if (e.getClickedBlock() == null) return;
            e.setCancelled(true);
            final Block block = e.getClickedBlock();
            final AbstractStruct struct = pair.getSecond();
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                struct.setFirstPoint(block.getLocation());
                p.sendMessage(Memories.PREFIX + "§eLe premier point est désormais défini en §b" + Utils.getFormattedLocation(block.getLocation()));
            } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                struct.setSecondPoint(block.getLocation());
                p.sendMessage(Memories.PREFIX + "§eLe deuxième point est désormais défini en §b" + Utils.getFormattedLocation(block.getLocation()));
            }
        } else if (material.equals(Material.NETHER_STAR)){
            e.setCancelled(true);
            final Location location = p.getLocation();
            final Arena arena = (Arena) pair.getSecond();
            arena.setSpawnPoint(location);
            p.sendMessage(Memories.PREFIX + "§eLe spawn point de l'arène est désormais défini en §b" + Utils.getFormattedLocation(location));
        }
    }
}
