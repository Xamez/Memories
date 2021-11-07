package fr.xamez.memories.commands;

import fr.xamez.memories.Memories;
import fr.xamez.memories.game.Game;
import fr.xamez.memories.struct.Arena;
import fr.xamez.memories.struct.Structure;
import fr.xamez.memories.listeners.ArenaEditorListener;
import fr.xamez.memories.runnables.HitBoxRunnable;
import fr.xamez.memories.runnables.ScoreboardRunnable;
import fr.xamez.memories.utils.FileUtils;
import fr.xamez.memories.utils.ItemBuilder;
import fr.xamez.memories.utils.Pair;
import fr.xamez.memories.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class MemoriesCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            final Player p = (Player) sender;
            final UUID uuid = p.getUniqueId();
            if (args.length == 0) {
                sendHelp(p);
            } else {
                String name = null;
                if (args.length == 2) {
                    name = args[1];
                }
                switch (args[0]){
                    case "createarena" -> {
                        if (name == null) {
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié le nom d'une arène !");
                        } else if (Utils.findArenaByName(name).isEmpty() && Utils.findStructureByName(name).isEmpty() ) {
                            Arena.ARENAS.add(new Arena(name));
                            p.sendMessage(Memories.PREFIX + "§eL'arène §a" + name + " §ea été correctement créé");
                            if (ArenaEditorListener.EDITOR_MODE.containsKey(uuid)) {
                                exitEditMode(ArenaEditorListener.EDITOR_MODE.get(uuid).getFirst().getName(), p);
                            }
                            editArena(name, p);
                            FileUtils.saveAllStruct(false);
                        } else {
                            p.sendMessage(Memories.PREFIX + "§cUne arène ou structure avec ce nom existe déjà !");
                        }
                    }
                    case "createstructure" -> {
                        if (name == null) {
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié le nom d'une structure !");
                        } else if (Utils.findArenaByName(name).isEmpty() && Utils.findStructureByName(name).isEmpty() ) {
                            Structure.STRUCTURES.add(new Structure(name));
                            p.sendMessage(Memories.PREFIX + "§eLa structure §a" + name + " §ea été correctement créé basé sur votre position");
                            if (ArenaEditorListener.EDITOR_MODE.containsKey(uuid)) {
                                exitEditMode(ArenaEditorListener.EDITOR_MODE.get(uuid).getFirst().getName(), p);
                            }
                            editStructure(name, p);
                            FileUtils.saveAllStruct(false);
                        } else {
                            p.sendMessage(Memories.PREFIX + "§cUne arène ou structure avec ce nom existe déjà !");
                        }
                    }
                    case "deletearena" -> {
                        if (name == null){
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié le nom d'une arène !");
                            return false;
                        }
                        final Optional<Arena> arena = Utils.findArenaByName(name);
                        if (arena.isPresent()) {
                            if (ArenaEditorListener.EDITOR_MODE.containsKey(uuid))
                                exitEditMode(name, p);
                            Arena.ARENAS.remove(arena.get());
                            FileUtils.clearFile("arenas");
                            FileUtils.saveAllStruct(false);
                            p.sendMessage(Memories.PREFIX + "§eL'arène §a" + name + " §ea été correctement supprimé");
                        } else {
                            p.sendMessage(Memories.PREFIX + "§cCette arène n'existe pas !");
                        }
                    }
                    case "deletestructure" -> {
                        if (name == null){
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié le nom d'une structure !");
                            return false;
                        }
                        final Optional<Structure> structure = Utils.findStructureByName(name);
                        if (structure.isPresent()) {
                            if (ArenaEditorListener.EDITOR_MODE.containsKey(uuid))
                                exitEditMode(name, p);
                            Structure.STRUCTURES.remove(structure.get());
                            FileUtils.clearFile("structures");
                            FileUtils.saveAllStruct(false);
                            p.sendMessage(Memories.PREFIX + "§eLa structure §a" + name + " §ea été correctement supprimé");
                        } else {
                            p.sendMessage(Memories.PREFIX + "§cCette structure n'existe pas !");
                        }
                    }
                    case "editarena" -> {
                        if (name == null)
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié le nom d'une arène !");
                        else
                            editArena(name, p);
                    }
                    case "editstructure" -> {
                        if (name == null)
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié le nom d'une structure !");
                        else
                            editStructure(name, p);
                    }
                    case "listarena" -> {
                        if (Arena.ARENAS.isEmpty()) {
                            p.sendMessage(Memories.PREFIX + "§eIl n'y a aucune arène !");
                        } else {
                            p.sendMessage(Memories.PREFIX + "§6» §eListe des arènes:");
                            int num = 1;
                            for (Arena arena : Arena.ARENAS) {
                                p.sendMessage("    §6" + num + ". §b" + arena.getName());
                                p.sendMessage("         §7⚫ §fPoint de spawn: §a" + Utils.getFormattedLocation(arena.getSpawnPoint()));
                                p.sendMessage("         §7⚫ §fPremier coin: §b" + Utils.getFormattedLocation(arena.getFirstPoint()));
                                p.sendMessage("         §7⚫ §fDeuxième coin §b" + Utils.getFormattedLocation(arena.getSecondPoint()));
                                p.sendMessage("         §7⚫ §fNombre de blocs: §d" + arena.getSize());
                                num++;
                            }
                        }
                    }
                    case "liststructure" -> {
                        if (Structure.STRUCTURES.isEmpty()) {
                            p.sendMessage(Memories.PREFIX + "§eIl n'y a aucune structure !");
                        } else {
                            p.sendMessage(Memories.PREFIX + "§6» §eListe des structures:");
                            int num = 1;
                            for (Structure structure : Structure.STRUCTURES) {
                                p.sendMessage("    §6" + num + ". §b" + structure.getName());
                                p.sendMessage("         §7⚫ §fPremier coin: §b" + Utils.getFormattedLocation(structure.getFirstPoint()));
                                p.sendMessage("         §7⚫ §fDeuxième coin §b" + Utils.getFormattedLocation(structure.getSecondPoint()));
                                p.sendMessage("         §7⚫ §fNombre de blocs: §d" + structure.getSize());
                                num++;
                            }
                        }
                    }

                    case "setspawn" -> {
                        Memories.GAME.setSpawnLocation(p.getLocation());
                        FileUtils.saveConfig(false);
                        p.sendMessage(Memories.PREFIX + "§eVous venez de définir le point de spawn du jeu en §a" + Utils.getFormattedLocation(p.getLocation()));
                    }
                    case "setradius" -> {
                        if (name == null) {
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié une taille de rayon");
                            return false;
                        }
                        try {
                            final double radius = Double.parseDouble(name);
                            Memories.GAME.setRadius(radius);
                            FileUtils.saveConfig(false);
                            p.sendMessage(Memories.PREFIX + "§eVous venez de définir le rayon à §a" + radius);
                        } catch (NumberFormatException ignored) {
                            p.sendMessage(Memories.PREFIX + "§cVous devez spécifié un rayon valide !");
                        }
                    }
                    case "reload" -> {
                        ScoreboardRunnable.BOARDS.clear();
                        for (UUID pUUID : ArenaEditorListener.EDITOR_MODE.keySet()) {
                            exitEditMode(ArenaEditorListener.EDITOR_MODE.get(pUUID).getFirst().getName(), Bukkit.getPlayer(pUUID));
                        }
                        Memories.GAME.setupAllPlayer();
                        Memories.GAME = new Game();
                    }
                    case "start" -> {
                        Memories.GAME.start();
                    }
                    case "stop" -> {
                        Memories.GAME.forceStop();
                    }
                }
            }
        } else {
            sender.sendMessage(Memories.PREFIX + "§cLa commande doit être utilisé depuis le jeu");
        }
        return false;
    }


    private void editArena(String name, Player p) {
        final UUID uuid = p.getUniqueId();
        final Optional<Arena> arena = Utils.findArenaByName(name);
        if (arena.isEmpty()) {
            p.sendMessage(Memories.PREFIX + "§cCette arène n'existe pas !");
            return;
        }
        if (ArenaEditorListener.EDITOR_MODE.containsKey(uuid)) {
            exitEditMode(name, p);
        } else {
            final Arena arenaCopy = arena.get().clone();
            HitBoxRunnable.HIT_BOX.add(arenaCopy);
            ArenaEditorListener.EDITOR_MODE.put(uuid, new Pair<>(arena.get(), arenaCopy));
            enableEditMode(p, true);
        }
    }

    private void editStructure(String name, Player p) {
        final UUID uuid = p.getUniqueId();
        final Optional<Structure> structure = Utils.findStructureByName(name);
        if (structure.isEmpty()) {
            p.sendMessage(Memories.PREFIX + "§cCette structure n'existe pas !");
            return;
        }
        if (ArenaEditorListener.EDITOR_MODE.containsKey(uuid)) {
            exitEditMode(name, p);
        } else {
            final Structure structureCopy = structure.get().clone();
            HitBoxRunnable.HIT_BOX.add(structureCopy);
            ArenaEditorListener.EDITOR_MODE.put(uuid, new Pair<>(structure.get(), structureCopy));
            enableEditMode(p, false);
        }
    }

    public static void exitEditMode(String name, Player p) {
        p.getInventory().clear();
        ArenaEditorListener.EDITOR_MODE.remove(p.getUniqueId());
        removeHitBoxByName(name);
        p.sendMessage(Memories.PREFIX + "§eVous venez de sortir du mode d'édition !");
    }

    private static void removeHitBoxByName(String name) {
        HitBoxRunnable.HIT_BOX.removeIf(struct -> struct.getName().equalsIgnoreCase(name));
    }

    private void enableEditMode(Player p, boolean isArena) {
        p.getInventory().clear();
        p.sendMessage(Memories.PREFIX + "§eVous venez de rentrer dans le mode d'édition !");
        final ItemStack barrier = new ItemBuilder(Material.BARRIER)
                .setName("§8» §cAnnuler l'édition")
                .toItemStack();
        p.getInventory().setItem(0, barrier);
        final ItemStack stick = new ItemBuilder(Material.STICK)
                .setName("§8» §bDéfinir la zone")
                .setLore(Arrays.asList("",
                        "§8⚫ §7Clic gauche pour définir le premier point",
                        "§8⚫ §7Clic droit pour définir le deuxième point"))
                .toItemStack();
        p.getInventory().setItem(3, stick);
        if (isArena) {
            final ItemStack netherStar = new ItemBuilder(Material.NETHER_STAR)
                    .setName("§8» §dDéfinir le point de spawn de l'arène")
                    .toItemStack();
            p.getInventory().setItem(4, netherStar);
        }
        final ItemStack slime = new ItemBuilder(Material.SLIME_BALL)
                .setName("§8» §aConfirmer l'édition")
                .toItemStack();
        p.getInventory().setItem(8, slime);
    }

    private void sendHelp(Player p) {
        p.sendMessage("");
        p.sendMessage("§8⬛ §6/memories createarena/createstructure <nom>");
        p.sendMessage("     §e» Permet de créer une arène/structure");
        p.sendMessage("§8⬛ §6/memories deletearena/deletestructure <nom>");
        p.sendMessage("     §e» Permet de supprimer une arène/structure");
        p.sendMessage("§8⬛ §6/memories editarena/editstructure <nom>");
        p.sendMessage("     §e» Permet de rentrer en mode d'édition");
        p.sendMessage("§8⬛ §6/memories listarena/liststructure");
        p.sendMessage("     §e» Permet de voir la liste des arènes/structures");
        p.sendMessage("§8⬛ §6/memories setspawn");
        p.sendMessage("     §e» Permet de définir le spawn l'évènement");
        p.sendMessage("§8⬛ §6/memories setradius");
        p.sendMessage("     §e» Permet de définir le radius de la worldboarder");
        p.sendMessage("§8⬛ §6/memories start");
        p.sendMessage("     §e» Permet de démarrer l'évènement");
        p.sendMessage("§8⬛ §6/memories stop");
        p.sendMessage("     §e» Permet de forcer l'arrêt de l'évènement");
        p.sendMessage("§8⬛ §6/memories reload");
        p.sendMessage("     §e» Permet de reload la partie");
        p.sendMessage("");
    }
}
