package fr.xamez.memories.game;

import fr.xamez.memories.Memories;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class GameClock extends BukkitRunnable {

    public static final HashMap<String, GameClock> GAME_CLOCKS = new HashMap<>();

    private final String name;
    private final int initSeconds;
    private int seconds;

    public GameClock(String name, int seconds){
        this.name = name;
        this.initSeconds = seconds;
        this.seconds = seconds;
        GAME_CLOCKS.put(name, this);
    }

    public void start(){
        this.runTaskTimerAsynchronously(JavaPlugin.getPlugin(Memories.class), 0L, 20L);
    }

    public void reset(){
        if (!this.isCancelled()) {
            this.seconds = initSeconds;
        }
    }

    @Override
    public void run(){
        for (Player p : Bukkit.getOnlinePlayers()){
            if (!p.getGameMode().equals(GameMode.SPECTATOR)) {
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6» §eTemps restant: §b" + getFormattedTime()));
                final String text = GameState.STARTING.name().equals(this.name) ? "Démarrage de la partie" : "Phase suivante";
                if (seconds == 30 || seconds == 20 || seconds == 10 || seconds <= 5)
                    p.sendTitle("§6" + text, "§eDans §b" + seconds + " secondes", 0, 25, 0);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6» §e" + text + " dans §b" + seconds + " secondes"));
                p.setLevel(seconds);
                p.setExp((float) seconds / initSeconds);
            }
        }
        if (this.seconds == 0){
            final JavaPlugin plugin = JavaPlugin.getPlugin(Memories.class);
            plugin.getServer().getScheduler().runTask(plugin, () -> Memories.GAME.nextPhase());
            this.cancel();
        }
        this.seconds--;
    }

    public String getFormattedTime(){
        final int secs = this.seconds < 0 ? this.seconds - 1 : this.seconds;
        return String.format("%02d:%02d", (secs % 3600) / 60, secs % 60);
    }

}