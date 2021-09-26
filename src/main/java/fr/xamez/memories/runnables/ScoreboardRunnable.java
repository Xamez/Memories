package fr.xamez.memories.runnables;

import fr.mrmicky.fastboard.FastBoard;
import fr.xamez.memories.arena.Arena;
import fr.xamez.memories.game.Game;
import fr.xamez.memories.game.GameClock;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

import static fr.xamez.memories.Memories.GAME;

public class ScoreboardRunnable extends BukkitRunnable {

    public static final HashMap<UUID, FastBoard> BOARDS = new HashMap<>();

    @Override
    public void run() {
        for (FastBoard board : BOARDS.values()){
            final Arena arena = Game.PLAYERS_ARENA.get(board.getPlayer());
            final String arenaName = (arena == null) ? "§7Non défini" : arena.getName();
            board.updateLines(
                    "",
                    "§6» §eÉtat du jeu: " + GAME.getGameState().getState(),
                    "§6» §eTemps restant: §b" + GameClock.GAME_CLOCKS.get(GAME.getGameState().name()).getFormattedTime(),
                    "§6» §eArène: §9" + arenaName,
                    "",
                    "     §aplay.ipduserver.fr"
            );
        }
    }
}
