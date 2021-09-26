package fr.xamez.memories;

import fr.xamez.memories.commands.MemoriesCMD;
import fr.xamez.memories.commands.MemoriesTAB;
import fr.xamez.memories.game.Game;
import fr.xamez.memories.listeners.ArenaEditorListener;
import fr.xamez.memories.listeners.PlayerListener;
import fr.xamez.memories.runnables.HitBoxRunnable;
import fr.xamez.memories.runnables.ScoreboardRunnable;
import fr.xamez.memories.utils.FileUtils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Memories extends JavaPlugin {

    public static final String PREFIX = "§8[§6Memories§8] §r";

    public static Game GAME;
    public static ConsoleCommandSender CONSOLE;
    public static File DATA_FOLDER;

    @Override
    public void onEnable() {
        CONSOLE = this.getServer().getConsoleSender();
        DATA_FOLDER = this.getDataFolder();
        GAME = new Game();
        GAME.setupAllPlayer(); // useful only when plugman reload
        registerListeners();
        registerCommands();
        runRunnables();
    }

    @Override
    public void onDisable() {
        FileUtils.saveAllStruct(false);
        FileUtils.saveConfig(true);
    }

    private void registerListeners(){
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new ArenaEditorListener(), this);
        pm.registerEvents(new PlayerListener(), this);
    }

    private void registerCommands(){
        this.getCommand("memories").setExecutor(new MemoriesCMD());
        this.getCommand("memories").setTabCompleter(new MemoriesTAB());
    }

    private void runRunnables(){
        new HitBoxRunnable().runTaskTimerAsynchronously(this, 0L, 5L);
        new ScoreboardRunnable().runTaskTimerAsynchronously(this, 0L, 20L);
    }
}
    