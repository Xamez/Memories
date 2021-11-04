package fr.xamez.memories.commands;

import fr.xamez.memories.struct.Arena;
import fr.xamez.memories.struct.Structure;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MemoriesTAB implements TabCompleter {

    private final List<String> COMMANDS = new ArrayList<>(Arrays.asList("createarena", "deletearena", "editarena",
                                                                        "listarena", "createstructure", "deletestructure",
                                                                        "editstructure", "liststructure", "setspawn",
                                                                        "setstructurespawn", "setradius", "reload",
                                                                        "start", "stop"));

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
            Collections.sort(completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("deletearena") || args[0].equalsIgnoreCase("editarena")) {
                StringUtil.copyPartialMatches(args[1], Arena.ARENAS.stream().map(Arena::getName).collect(Collectors.toList()), completions);
            }
            else if (args[0].equalsIgnoreCase("deletestructure") || args[0].equalsIgnoreCase("editstructure"))
                StringUtil.copyPartialMatches(args[1], Structure.STRUCTURES.stream().map(Structure::getName).collect(Collectors.toList()), completions);
            Collections.sort(completions);
        }
        return completions;
    }
}
