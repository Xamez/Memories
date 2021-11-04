package fr.xamez.memories.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.xamez.memories.Memories;
import fr.xamez.memories.struct.AbstractStruct;
import fr.xamez.memories.struct.Arena;
import fr.xamez.memories.struct.Structure;
import org.bukkit.Location;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class FileUtils {

    private static final Gson GSON = new GsonBuilder().serializeNulls()
                                                      .setPrettyPrinting()
                                                      .disableHtmlEscaping()
                                                      .registerTypeAdapter(Location.class, new LocationAdapter())
                                                      .create();
    private static final File CONFIG_FILE = new File(Memories.DATA_FOLDER, "config.json");
    private static final File ARENA_FILE = new File(Memories.DATA_FOLDER, "arenas.json");
    private static final File STRUCTURE_FILE = new File(Memories.DATA_FOLDER, "structures.json");

    public static void createDefaultJsonFiles() {
        createJsonFile(CONFIG_FILE);
        createJsonFile(ARENA_FILE);
        createJsonFile(STRUCTURE_FILE);
    }

    public static void createJsonFile(File file) {
        final File folder = Memories.DATA_FOLDER;
        try {
            if (!folder.exists()) folder.mkdir();
            if (!file.exists()) file.createNewFile();
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de créer le fichier §4'" + file.getName() + "'");
        }
    }

    public static void clearFile(String filename) {
        final File file = new File(Memories.DATA_FOLDER, filename + ".json");
        if (!file.exists()) createJsonFile(file);
        try {
            final PrintWriter writer = new PrintWriter(file);
            writer.print("");
            writer.close();
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de supprimer le contenu du fichier §4'" + file.getName() + "'");
        }
    }

    public static void saveAllStruct(boolean debug) {
        createDefaultJsonFiles();
        try {
            if (Arena.ARENAS.isEmpty()) {
                if (debug) Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cAucune arène enregistré !");
            } else {
                final FileWriter arenaWriter = new FileWriter(ARENA_FILE);
                arenaWriter.write(GSON.toJson(Arena.ARENAS));
                if (debug) Memories.CONSOLE.sendMessage(Memories.PREFIX + "§b" + Arena.ARENAS.size() + " §earènes enregistré");
                arenaWriter.close();
            }
            if (Structure.STRUCTURES.isEmpty()) {
                if (debug) Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cAucune structure enregistré !");
                return;
            }
            final FileWriter structureWriter = new FileWriter(STRUCTURE_FILE);
            structureWriter.write(GSON.toJson(Structure.STRUCTURES));
            if (debug) Memories.CONSOLE.sendMessage(Memories.PREFIX + "§b" + Structure.STRUCTURES.size() + " §estructures enregistré");
            structureWriter.close();
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible d'enregistrer toutes les structures et arènes");
        }
    }

    public static void saveConfig(boolean debug) {
        try {
            final FileWriter writer = new FileWriter(CONFIG_FILE);
            writer.write(GSON.toJson(Memories.GAME.getConfig()));
            if (debug) Memories.CONSOLE.sendMessage(Memories.PREFIX + "§eConfiguration enregistré");
            writer.close();
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de charger la config");
        }
    }

    public static Config loadConfig() {
        try {
            final FileReader reader = new FileReader(CONFIG_FILE);
            Config config = GSON.fromJson(reader, Config.class);
            if (config == null) config = new Config(AbstractStruct.DEFAULT_LOCATION, 0);
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§eConfiguration chargé");
            reader.close();
            return config;
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de charger la config");
        }
        return null;
    }

    public static ArrayList<Arena> loadArenas() {
        try {
            final FileReader reader = new FileReader(ARENA_FILE);
            final Type type = new TypeToken<ArrayList<Arena>>(){}.getType();
            ArrayList<Arena> arenas = GSON.fromJson(reader, type);
            if (arenas == null) arenas = new ArrayList<>();
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§b" + arenas.size() + " §earènes chargé");
            arenas.forEach(Arena::updateBlockList);
            reader.close();
            return arenas;
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de charger les arènes");
        }
        return new ArrayList<>();
    }

    public static ArrayList<Structure> loadStructures() {
        try {
            final FileReader reader = new FileReader(STRUCTURE_FILE);
            final Type type = new TypeToken<ArrayList<Structure>>(){}.getType();
            ArrayList<Structure> structures = GSON.fromJson(reader, type);
            if (structures == null) structures = new ArrayList<>();
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§b" + structures.size() + " §estructures chargé");
            structures.forEach(Structure::updateBlockList);
            reader.close();
            return structures;
        } catch (IOException ignored) {
            Memories.CONSOLE.sendMessage(Memories.PREFIX + "§cImpossible de charger les structures");
        }
        return new ArrayList<>();
    }

}
