package fr.codinbox.footballplugin.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.Arena;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.service.MinecraftWorldService;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.SerializablePosition;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class MinecraftWorldServiceImpl implements MinecraftWorldService {

    private static final Logger LOGGER = Bukkit.getLogger();

    private final File worldsDirectory;
    private final File mapsFile;
    private ArrayList<FootMap> maps;
    private HashMap<MinecraftWorld, Boolean> loadedWorlds;

    private int mapNameIncrement;

    public MinecraftWorldServiceImpl(File worldsDirectory, File mapsFile) {
        this.worldsDirectory = worldsDirectory;
        this.mapsFile = mapsFile;
    }

    @Override
    public void init(Plugin plugin) {
        // Init fields
        this.maps = new ArrayList<>();
        this.loadedWorlds = new HashMap<>();
        this.mapNameIncrement = 1;

        // Load all maps
        try {
            loadMaps(mapsFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void loadMaps(File mapsFile) throws IOException {
        LOGGER.info("[Football] Loading maps");

        boolean needToSave = false;

        FootMapCapacitor footMapCapacitor;

        // Maps file not found
        if(!mapsFile.exists()) {
            footMapCapacitor = new FootMapCapacitor(new ArrayList<>());
            needToSave = true;
        }

        // File found, loading it
        else
            footMapCapacitor = new Gson().fromJson(new FileReader(mapsFile), FootMapCapacitor.class);

        this.maps.addAll(footMapCapacitor.getMaps());

        if(needToSave)
            saveMaps();

        LOGGER.info("[Football] Maps successfully loaded!");
    }

    @Override
    public void exit() {
        LOGGER.info("[Football] Closing loaded maps...");

        // Unload all world
        ArrayList<MinecraftWorld> mapsToUnload = new ArrayList<>(loadedWorlds.keySet());
        for(MinecraftWorld minecraftWorld : mapsToUnload)
            unloadWorld(minecraftWorld);

        // Save all maps into data folders
        saveMaps();

        LOGGER.fine("[Football] Close complete!");
    }

    @Override
    public FootMap createMap(String name) {
        if(getMapByName(name) != null)
            throw new IllegalArgumentException("Map '" + name + "' is already created");

        FootMap map = new FootMap(name, FootMode.CASUAL__TWO_VERSUS_TWO, new HashMap<FootTeamQualifier, ArrayList<SerializablePosition>>() {{
            put(FootTeamQualifier.BLUE, new ArrayList<>());
            put(FootTeamQualifier.RED, new ArrayList<>());
        }});

        this.maps.add(map);

        LOGGER.info("[Football] Created foot map '" + name + "'");

        return map;
    }

    @Override
    public void deleteMap(FootMap map) {
        if(map == null)
            return;

        ArrayList<Arena> arenasToClose = new ArrayList<>();

        Iterator<MinecraftWorld> worldIterator = getWorlds(map);
        while(worldIterator.hasNext()) {
            MinecraftWorld world = worldIterator.next();
            for(Arena arena : FootballPlugin.INSTANCE.getArenaManager().getArenas())
                if(arena.getMap().equals(world))
                    arenasToClose.add(arena);
        }

        for(Arena arena : arenasToClose) {
            arena.getPlayerExecutor().broadcastMessage("§cThis arena has been closed.");
            arena.endMatch();
        }

        map.getSaveDirectory().delete();

        this.maps.remove(map);
    }

    @Override
    public FootMap getMapByName(String name) {
        for(FootMap map : this.maps)
            if(map.getName().equals(name))
                return map;
        return null;
    }

    @Override
    public Iterator<FootMap> getMaps() {
        return maps.iterator();
    }

    @Override
    public Iterator<FootMap> getMaps(FootMode mode) {
        ArrayList<FootMap> maps = new ArrayList<>();
        for (FootMap map : this.maps) {
            if(mode.equals(map.getMode()))
                maps.add(map);
        }
        return maps.iterator();
    }

    @Override
    public void saveMaps() {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(this.mapsFile), StandardCharsets.UTF_8);
            writer.write(gson.toJson(new FootMapCapacitor(this.maps)));
            writer.flush();
            writer.close();
            LOGGER.info("[Football] Maps file saved!");
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public MinecraftWorld loadWorld(FootMap map, boolean editMode) {
        MinecraftWorld createdWorld = null;

        try {
            if (editMode) {
                MinecraftWorld world = getEditingWorld(map.getMode());
                if(world != null)
                    return world;
            }

            LOGGER.info("[Football] Loading map '" + map.getName() + "'");

            boolean newWorld = false;

            String worldName = formatWorldDirectoryName(map.getName(), getNextMapIncrement()) + "/";

            File mapSaveDirectory = map.getSaveDirectory();
            File mapDirectory = new File(worldName + "/");
            if (!mapSaveDirectory.exists())
                newWorld = true;
            else
                FileUtils.copyDirectory(map.getSaveDirectory(), mapDirectory);

            // Delete uid.dat file to prevent map loading prevention
            File uid = new File(mapDirectory, "uid.dat");
            if (uid.exists())
                uid.delete();

            MinecraftWorld minecraftWorld = new MinecraftWorld(worldName, map);
            minecraftWorld.create(newWorld);

            loadedWorlds.put(minecraftWorld, editMode);

            createdWorld = minecraftWorld;
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        return createdWorld;
    }

    private String formatWorldDirectoryName(String mapName, int increment) {
        return mapName + "_" + increment;
    }

    private int getNextMapIncrement() {
        int increment = this.mapNameIncrement;
        mapNameIncrement++;
        return increment;
    }

    @Override
    public void unloadWorld(MinecraftWorld world) {
        String mapName = world.getName();

        LOGGER.info("[Football] Unloading world '" + mapName + "'");
        boolean isBuildWorld = loadedWorlds.get(world);

        if(world.isCreated())
            world.destroy(isBuildWorld);

        // Save world in worlds directory if this world is a build world
        // Plus delete the world directory
        try {
            if(isBuildWorld) {
                FileUtils.copyDirectory(world.getMapDirectory(), new File(this.worldsDirectory, world.getParentMap().getName()));
                LOGGER.info("[Football] Saved world '" + world.getParentMap().getName() + "'");
            }

            FileUtils.deleteDirectory(world.getMapDirectory());
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        this.loadedWorlds.remove(world);
    }

    @Override
    public Iterator<MinecraftWorld> getWorlds() {
        return this.loadedWorlds.keySet().iterator();
    }

    @Override
    public Iterator<MinecraftWorld> getWorlds(FootMode mode) {
        final ArrayList<MinecraftWorld> maps = new ArrayList<>();
        this.loadedWorlds.forEach((minecraftMap, isBuildWorld) -> {
            if(minecraftMap.getParentMap() != null && minecraftMap.getParentMap().getMode().equals(mode))
                maps.add(minecraftMap);
        });
        return maps.iterator();
    }

    @Override
    public Iterator<MinecraftWorld> getWorlds(FootMap map) {
        final ArrayList<MinecraftWorld> maps = new ArrayList<>();
        this.loadedWorlds.forEach((minecraftMap, isBuildWorld) -> {
            if(minecraftMap.getParentMap() != null && minecraftMap.getParentMap().equals(map))
                maps.add(minecraftMap);
        });
        return maps.iterator();
    }

    @Override
    public MinecraftWorld getEditingWorld(FootMode mode) {
        for(Map.Entry<MinecraftWorld, Boolean> entry : this.loadedWorlds.entrySet()) {
            if(entry.getKey().getParentMap().getMode().equals(mode) && entry.getValue())
                return entry.getKey();
        }
        return null;
    }

    @Override
    public MinecraftWorld getWorldByBukkitWorld(World world) {
        for(MinecraftWorld minecraftWorld : this.loadedWorlds.keySet())
            if(minecraftWorld.getWorld().equals(world))
                return minecraftWorld;
        return null;
    }

}
