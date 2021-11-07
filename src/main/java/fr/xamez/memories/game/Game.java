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
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.xamez.memories.Memories.GAME;


public class Game {

    public final static HashMap<Player, Arena> PLAYERS_ARENA = new HashMap<>();
    public final static HashMap<Player, HashMap<Arena, Float>> PLAYERS_RESULT = new HashMap<>();

    private GameState gameState;
    private final GameClock startingClock = new GameClock(GameState.STARTING.name(), 5);
    private final GameClock generationClock = new GameClock(GameState.MEMORIZATION.name(), 2);
    private final GameClock memorizeClock = new GameClock(GameState.MEMORIZATION.name(), 10);
    private final GameClock buildClock = new GameClock(GameState.BUILDING.name(), 20);
    private final GameClock waitClock = new GameClock(GameState.WAITING.name(), 10);
    private final GameClock finishClock = new GameClock(GameState.FINISHED.name(), 300);
    private final ArrayList<Structure> playedStructure = new ArrayList<>();
    private final Config config;
    private final int maxTurn;
    private int turn;
    private GameClock currentClock;
    private Structure currentStructure;
    private boolean canBuild;
    private boolean canJoin;

    public Game() {
        FileUtils.createDefaultJsonFiles();
        this.config = FileUtils.loadConfig();

        Arena.ARENAS = FileUtils.loadArenas();
        Structure.STRUCTURES = FileUtils.loadStructures();

        this.gameState = GameState.WAITING;
        this.currentClock = this.waitClock;

        this.canBuild = false;
        this.turn = 0;
        this.maxTurn = Structure.STRUCTURES.size();

        final WorldBorder worldBorder = this.getConfig().getSpawnLocation().getWorld().getWorldBorder();
        worldBorder.setCenter(this.getConfig().getSpawnLocation());
        worldBorder.setSize(this.getConfig().getWorldBoarderRadius());
    }

    public void start() {
        resetClocks();
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
        this.currentClock = this.startingClock;
        GameClock.CAN_START = true;
    }

    public void forceStop() {
        resetClocks();
        this.gameState = GameState.FINISHED;
        this.currentClock = this.finishClock;
        GameClock.CAN_START = true;
        Bukkit.broadcastMessage(Memories.PREFIX + "§cLa partie a été stoppée de force");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
            p.teleport(getConfig().getSpawnLocation());
        }
    }

    public void nextPhase() {
        this.currentClock.cancel();
        switch (this.gameState){
            case STARTING -> {
                this.gameState = GameState.GENERATION;
                this.currentClock = this.generationClock;
                startGenerationPhase();
            }
            case GENERATION -> {
                this.gameState = GameState.MEMORIZATION;
                this.currentClock = this.memorizeClock;
                startMemorizationPhase();
            }
            case MEMORIZATION -> {
                this.gameState = GameState.BUILDING;
                this.currentClock = this.buildClock;
                startBuildingPhase();
            }
            case BUILDING -> {
                this.gameState = GameState.WAITING;
                this.currentClock = this.waitClock;
                startWaitingPhase();
            }
            case WAITING -> {
                resetClocks();
                if (this.turn < this.maxTurn) {
                    this.gameState = GameState.MEMORIZATION;
                    this.currentClock = this.memorizeClock;
                    startMemorizationPhase();
                } else {
                    this.gameState = GameState.FINISHED;
                    this.currentClock = this.finishClock;
                    this.canJoin = true;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.getInventory().clear();
                        p.teleport(getConfig().getSpawnLocation());
                    }
                    // DISPLAY GLOBAL RESULT
                    Bukkit.broadcastMessage(Memories.PREFIX + "§aLes résultats sont :");
                    AtomicInteger i = new AtomicInteger(1);
                    final Map<Player, Integer> scores = new HashMap<>();
                    for (Player player : PLAYERS_RESULT.keySet()) {
                        float pScore = 0;
                        for (Map.Entry<Arena, Float> entry : PLAYERS_RESULT.get(player).entrySet())
                            pScore += entry.getValue();
                        scores.put(player, (int) (pScore / PLAYERS_RESULT.get(player).size()));
                    }
                    scores.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                            .forEach(e -> {
                                Bukkit.broadcastMessage("  §f" + i + "§7. §e" + e.getKey().getName() + " §7- §b" + e.getValue() + "% en moyenne");
                                i.getAndIncrement();
                            });
                            //.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    Bukkit.broadcastMessage(Memories.PREFIX + "§eEvent §aterminé§e, merci à tous !!");
                    Bukkit.broadcastMessage(Memories.PREFIX + "§eLe serveur va s'éteindre dans §b" + this.currentClock.getFormattedTime());
                }
            }
            case FINISHED -> {
                for (Player p : Bukkit.getOnlinePlayers())
                    p.kickPlayer(Memories.PREFIX + "§eL'event est terminé ! Merci à tous d'avoir joué !");
                Bukkit.getServer().shutdown();
            }
        }
        // The code below is used to sync the game clock runnable with the scoreboard runnable
        GameClock.CAN_START = true;
    }

    private void resetClocks() {
        try {
            this.startingClock.reset();
            this.generationClock.reset();
            this.memorizeClock.reset();
            this.buildClock.reset();
            this.waitClock.reset();
            this.finishClock.reset();
        } catch (Exception ignored) {}
    }

    private void startGenerationPhase() {

        int i = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getGameMode().equals(GameMode.SPECTATOR)) {
                final Arena arena = Arena.ARENAS.get(i);
                PLAYERS_ARENA.put(p, arena);
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

        for (Arena arena : PLAYERS_ARENA.values())
            currentStructure.spawnStructure(arena.getFirstPoint().getBlock().getLocation());

        Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(Memories.class), () -> {
            for (FallingBlock fallingBlock : Structure.FALLING_BLOCKS_STRUCTURE)
                fallingBlock.teleport(fallingBlock.getLocation().clone().add(0, 20, 0));
        } , 60L);
    }

    private void startMemorizationPhase() {
        for (Map.Entry<Player, Arena> entry : PLAYERS_ARENA.entrySet()) {
            final Player p = entry.getKey();
            p.setAllowFlight(true);
            p.setFlying(true);
            p.teleport(entry.getValue().getSpawnPoint());
            p.sendMessage(Memories.PREFIX + "§eVous avez été téléporté à l'arène §b" + entry.getValue().getName());
        }
    }

    private void startBuildingPhase() {
        this.canBuild = true;
        for (Map.Entry<Player, Arena> entry : PLAYERS_ARENA.entrySet()) {
            final Player p = entry.getKey();
            p.setAllowFlight(false);
            p.setFlying(false);
            entry.getValue().destroyEffect(p);
        }
    }

    private void startWaitingPhase() {
        this.canBuild = false;
        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(Memories.class), () -> {
            for (Map.Entry<Player, Arena> entry : PLAYERS_ARENA.entrySet()) {
                final Player p = entry.getKey();
                final Arena arena = entry.getValue();
                float result = arena.compare(currentStructure);
                PLAYERS_RESULT.get(p).put(arena, result);
                p.getInventory().clear();
                p.sendMessage(Memories.PREFIX + "§eVous avez fait un score de §b" + result + "§7/§b100");
            }
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Memories.class), () -> {
                for (Arena arena : Arena.ARENAS)
                    arena.clearArena();
            });
        });
        this.turn++;
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
                "§6» §eTemps restant: §b" + GAME.getCurrentGameClock().getFormattedTime(),
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

    public GameClock getCurrentGameClock() {
        return this.currentClock;
    }
}