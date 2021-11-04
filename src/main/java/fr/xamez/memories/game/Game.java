package fr.xamez.memories.game;

import fr.mrmicky.fastboard.FastBoard;
import fr.xamez.memories.Memories;
import fr.xamez.memories.runnables.ScoreboardRunnable;
import fr.xamez.memories.struct.AbstractStruct;
import fr.xamez.memories.struct.Arena;
import fr.xamez.memories.struct.Structure;
import fr.xamez.memories.utils.Config;
import fr.xamez.memories.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class Game {

    public final static HashMap<Player, Arena> PLAYERS_ARENA = new HashMap<>();
    public final static HashMap<Player, HashMap<Arena, Float>> PLAYERS_RESULT = new HashMap<>();

    private GameState gameState;
    private final GameClock starting;
    private final GameClock memorizeClock;
    private final GameClock buildClock;
    private final GameClock waitClock;
    private final GameClock finishClock;
    private final ArrayList<Structure> playedStructure = new ArrayList<>();
    private Config config;
    private Structure currentStructure;
    private boolean canBuild;
    private boolean canJoin;
    private int turn;
    private int maxTurn;

    public Game() {
        init();
        this.starting = new GameClock(GameState.STARTING.name(), 5);
        this.memorizeClock = new GameClock(GameState.MEMORIZATION.name(), 10);
        this.buildClock = new GameClock(GameState.BUILDING.name(), 40);
        this.waitClock = new GameClock(GameState.WAITING.name(), 10);
        this.finishClock = new GameClock(GameState.FINISHED.name(), 300);
    }

    public void init() {
        FileUtils.createDefaultJsonFiles();
        this.config = FileUtils.loadConfig();

        Arena.ARENAS = FileUtils.loadArenas();
        Structure.STRUCTURES = FileUtils.loadStructures();

        this.gameState = GameState.WAITING;
        this.turn = 0;
        this.maxTurn = Structure.STRUCTURES.size();

        final WorldBorder worldBorder = this.getConfig().getSpawnLocation().getWorld().getWorldBorder();
        worldBorder.setCenter(this.getConfig().getSpawnLocation());
        worldBorder.setSize(this.getConfig().getWorldBoarderRadius());
    }

    public void start() {
        cancelClocks();
        if (Bukkit.getOnlinePlayers().stream().filter(p -> p.getGameMode().equals(GameMode.SURVIVAL)).count() > Arena.ARENAS.size()) {
            Bukkit.broadcastMessage(Memories.PREFIX + "§cLa partie ne peut pas commencer car il manque des arènes");
            return;
        }
        if (Structure.STRUCTURES.isEmpty()) {
            Bukkit.broadcastMessage(Memories.PREFIX + "§cLa partie ne peut pas commencer car il n'y a aucune structures");
            return;
        }
        this.canJoin = false;
        this.gameState = GameState.STARTING;
        this.starting.start();
    }

    public void forceStop() {
        resetClocks();
        cancelClocks();
        this.gameState = GameState.FINISHED;
        finishClock.start();
        Bukkit.broadcastMessage(Memories.PREFIX + "§cLa partie a été stoppée de force");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            p.teleport(getConfig().getSpawnLocation());
        }
    }

    // We didn't cancel the finishClock
    private void cancelClocks() {
        try {
            this.memorizeClock.cancel();
            this.waitClock.cancel();
            this.buildClock.cancel();
        } catch (Exception ignored) {}
    }

    public void nextPhase() {
        switch (this.gameState){
            case STARTING -> {
                this.gameState = GameState.MEMORIZATION;
                cancelClocks();
                startMemorizationPhase();
            }
            case MEMORIZATION -> {
                this.gameState = GameState.BUILDING;
                cancelClocks();
                startBuildingPhase();
            }
            case BUILDING -> {
                this.gameState = GameState.WAITING;
                cancelClocks();
                startWaitingPhase();
            }
            case WAITING -> {
                cancelClocks();
                if (this.turn <= this.maxTurn) {
                    this.gameState = GameState.MEMORIZATION;
                    resetClocks();
                    startMemorizationPhase();
                } else {
                    this.gameState = GameState.FINISHED;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.getInventory().clear();
                        p.teleport(getConfig().getSpawnLocation());
                    }
                    // DISPLAY GLOBAL RESULT
                    Bukkit.broadcastMessage(Memories.PREFIX + "§aLes résultats sont :");
                    AtomicInteger i = new AtomicInteger();
                    PLAYERS_RESULT.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                            .forEach(e -> {
                                Bukkit.broadcastMessage("  §f" + i + "§7. §e" + e.getKey().getName() + " §7: §b" + e.getValue());
                                i.getAndIncrement();
                            });
                            //.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    Bukkit.broadcastMessage(Memories.PREFIX + "§eEvent §aterminé§e, merci à tous !!");
                    Bukkit.broadcastMessage(Memories.PREFIX + "§eLe serveur va s'éteindre dans " + this.finishClock.getFormattedTime());
                    this.finishClock.start();
                }
            }
            case FINISHED -> {
                for (Player p : Bukkit.getOnlinePlayers())
                    p.kickPlayer(Memories.PREFIX + "§eL'event est terminé ! Merci à tous d'avoir joué !");
                Bukkit.getServer().shutdown();
            }
        }
    }

    private void resetClocks() {
        this.memorizeClock.reset();
        this.buildClock.reset();
        this.waitClock.reset();
        this.finishClock.reset();
    }

    private void startMemorizationPhase() {
        int i = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getGameMode().equals(GameMode.SPECTATOR)) {
                final Arena arena = Arena.ARENAS.get(i);
                PLAYERS_ARENA.put(p, arena);
                p.teleport(arena.getSpawnPoint());
                p.sendMessage(Memories.PREFIX + "§eVous avez été téléporté à l'arène §b" + arena.getName()); // fake teleport to their arena
                i++;
            }
        }

        final ArrayList<Structure> structures = new ArrayList<>(Structure.STRUCTURES);
        Collections.shuffle(structures);
        for (Structure structure : structures) {
            if (!playedStructure.contains(structure)) {
                this.currentStructure = structure;
                playedStructure.add(structure);
                break;
            }
        }
        for (Map.Entry<Player, Arena> entry : PLAYERS_ARENA.entrySet()) {
            currentStructure.spawnStructure(entry.getKey(), entry.getValue().getFirstPoint().getBlock().getLocation());
            System.out.println(entry.getKey() + " -> " + entry.getValue().getFirstPoint().getBlock().getLocation());
        }

        this.memorizeClock.start();
    }

    private void startBuildingPhase() {
        this.canBuild = true;
        this.buildClock.start();
        for (Map.Entry<Player, Arena> entry : PLAYERS_ARENA.entrySet()) {
            entry.getValue().destroyEffect(entry.getKey());
        }
    }

    // TODO EMLPECHER LE BUILD
    // TODO LINK SB ACTION BAR

    private void startWaitingPhase() {
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Memories.class), () -> {
            for (Map.Entry<Player, Arena> entry : PLAYERS_ARENA.entrySet()) {
                final Player p = entry.getKey();
                final Arena arena = entry.getValue();
                float result = arena.compare(currentStructure);
                PLAYERS_RESULT.get(p).put(arena, result);
                p.getInventory().clear();
                p.sendMessage(Memories.PREFIX + "§eVous avez fait un score de §b" + result + "§7/§b100");
            }
        });
        for (Arena arena : Arena.ARENAS){
            arena.clearArena();
        }
        this.turn++;
        this.waitClock.start();
    }

    public void setupAllPlayer() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PLAYERS_RESULT.put(p, new HashMap<>());
            setupPlayer(p);
        }
    }

    public void setupPlayer(Player p) {
        if (this.getConfig().getSpawnLocation() != AbstractStruct.DEFAULT_LOCATION)
            p.teleport(this.getConfig().getSpawnLocation());
        p.setHealth(20f);
        p.setSaturation(20f);
        p.setGameMode(GameMode.SURVIVAL);
        p.setLevel(0);
        p.setExp(0f);
        p.getInventory().clear();

        setupScoreboard(p);
    }

    private void setupScoreboard(Player p) {
        final FastBoard board = new FastBoard(p);
        board.updateTitle(Memories.PREFIX);
        board.updateLines(
                "",
                "§6» §eÉtat du jeu: " + this.gameState.getState(),
                "§6» §eTemps restant: §b" + GameClock.GAME_CLOCKS.get(this.gameState.name()).getFormattedTime(),
                "§6» §eArène: §7Non défini",
                "",
                "     §aplay.capsurgrimtown.fr"
        );
        ScoreboardRunnable.BOARDS.put(p.getUniqueId(), board);
    }

    public boolean canJoin() {
        return this.canJoin;
    }

    public int getTurn() {
        return turn;
    }

    public int getMaxTurn() {
        return maxTurn;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public void setCanBuild(boolean canBuild) {
        this.canBuild = canBuild;
    }

    public Structure getCurrentStructure() {
        return currentStructure;
    }

    public void setCurrentStructure(Structure currentStructure) {
        this.currentStructure = currentStructure;
    }

    public void setRadius(double radius) {
        this.getConfig().getSpawnLocation().getWorld().getWorldBorder().setSize(radius);
        this.getConfig().setWorldBoarderRadius(radius);
    }

    public void setSpawnLocation(Location location) {
        this.getConfig().getSpawnLocation().getWorld().getWorldBorder().setCenter(location);
        this.getConfig().setSpawnLocation(location);
    }

    public Config getConfig() {
        return config;
    }
}