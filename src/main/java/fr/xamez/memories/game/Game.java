package fr.xamez.memories.game;

import fr.mrmicky.fastboard.FastBoard;
import fr.xamez.memories.Memories;
import fr.xamez.memories.arena.AbstractStruct;
import fr.xamez.memories.arena.Arena;
import fr.xamez.memories.arena.Structure;
import fr.xamez.memories.runnables.ScoreboardRunnable;
import fr.xamez.memories.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Game {

    public final static HashMap<Player, Arena> PLAYERS_ARENA = new HashMap<>();

    private GameState gameState;
    private final GameClock starting;
    private final GameClock memorizeClock;
    private final GameClock buildClock;
    private final GameClock waitClock;
    private Structure currentStructure;
    private boolean join;
    private int turn;
    private int maxTurn;
    private Location spawnLocation;
    private double radius;

    public Game(){
        init();
        this.starting = new GameClock(GameState.STARTING.name(), 10);
        this.memorizeClock = new GameClock(GameState.MEMORIZATION.name(), 5);
        this.buildClock = new GameClock(GameState.BUILDING.name(), 10);
        this.waitClock = new GameClock(GameState.WAITING.name(), 20);
    }

    public void init(){
        this.gameState = GameState.WAITING;
        this.turn = 0;
        FileUtils.createDefaultJsonFiles();
        final Config config = FileUtils.loadConfig();
        this.spawnLocation = config.getLocation();
        this.radius = config.getRadius();
        final WorldBorder worldBorder = this.spawnLocation.getWorld().getWorldBorder();
        worldBorder.setCenter(this.spawnLocation);
        worldBorder.setSize(this.radius);
        Arena.ARENAS = FileUtils.loadArenas();
        Structure.STRUCTURES = FileUtils.loadStructures();
        this.maxTurn = Structure.STRUCTURES.size();
    }

    public void start(){
        if (Bukkit.getOnlinePlayers().stream().filter(p -> p.getGameMode().equals(GameMode.ADVENTURE)).count() > Arena.ARENAS.size()){
            Bukkit.broadcastMessage(Memories.PREFIX + "§cLa partie ne peut pas commencer car il manque des arènes");
            return;
        }
        if (Structure.STRUCTURES.isEmpty()){
            Bukkit.broadcastMessage(Memories.PREFIX + "§cLa partie ne peut pas commencer car il n'y a aucune structures");
            return;
        }
        this.join = false;
        this.gameState = GameState.STARTING;
        this.starting.start();
    }

    public void nextPhase(){
        switch (this.gameState){
            case STARTING -> {
                this.gameState = GameState.MEMORIZATION;
                startMemorizationPhase();
            }
            case MEMORIZATION -> {
                this.gameState = GameState.BUILDING;
                startBuildingPhase();
            }
            case BUILDING -> {
                this.gameState = GameState.WAITING;
                startWaitingPhase();
            }
            case WAITING -> {
                if (this.turn <= this.maxTurn) {
                    this.memorizeClock.reset();
                    this.buildClock.reset();
                    this.waitClock.reset();
                    startMemorizationPhase();
                } else {
                    Bukkit.broadcastMessage(Memories.PREFIX + "§eEvent §aterminé§e, merci à tous !!");
                }
            }
        }
    }

    private void startMemorizationPhase(){
        int i = 0;
        for (Player p : Bukkit.getOnlinePlayers()){
            if (!p.getGameMode().equals(GameMode.SPECTATOR)){
                final Arena arena = Arena.ARENAS.get(i);
                PLAYERS_ARENA.put(p, arena);
                p.teleport(arena.getSpawnPoint());
                p.sendMessage(Memories.PREFIX + "§eVous avez été téléporté à l'arène §b" + arena.getName());
                i++;
            }
        }

        // place structures

    }

    private void startBuildingPhase(){
        // play destroy effect
    }

    private void startWaitingPhase(){
        // teleport back to main lobby (or not ?)
        // see scores
        // add 1 to turn
    }

    public void setupAllPlayer(){
        for (Player p : Bukkit.getOnlinePlayers()){
            setupPlayer(p);
        }
    }

    public void setupPlayer(Player p){
        if (this.spawnLocation != AbstractStruct.DEFAULT_LOCATION)
            p.teleport(this.spawnLocation);
        p.setHealth(20f);
        p.setSaturation(20f);
        //p.setGameMode(GameMode.ADVENTURE);
        p.setLevel(0);
        p.setExp(0f);
        setupScoreboard(p);
    }

    private void setupScoreboard(Player p){
        final FastBoard board = new FastBoard(p);
        board.updateTitle(Memories.PREFIX);
        board.updateLines(
                "",
                "§6» §eÉtat du jeu: " + this.gameState.getState(),
                "§6» §eTemps restant: §b" + GameClock.GAME_CLOCKS.get(this.gameState.name()).getFormattedTime(),
                "§6» §eArène: §7Non défini",
                "",
                "     §aplay.ipduserver.fr"
        );
        ScoreboardRunnable.BOARDS.put(p.getUniqueId(), board);
    }

    public boolean canJoin(){
        return this.join;
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

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.spawnLocation.getWorld().getWorldBorder().setSize(radius);
        this.radius = radius;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation.getWorld().getWorldBorder().setCenter(location);
        this.spawnLocation = location;
    }
}