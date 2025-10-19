package fr.codinbox.footballplugin.map;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.util.Random;

public class MinecraftWorld {

    /**
     * Map name (same as the Bukkit one)
     */
    private final String name;

    /**
     * Parent map
     */
    private final FootMap parentMap;

    /**
     * Bukkit map referenced by the actual map
     */
    private World world;

    protected MinecraftWorld(String name, FootMap parentMap) {
        this.name = name;
        this.parentMap = parentMap;
    }

    public MinecraftWorld(String name) {
        this(name, null);
    }

    /**
     * Create the map and initialize the Bukkit world
     */
    public void create(boolean isNewWorld) {
        WorldCreator worldCreator = new WorldCreator(this.name);

        // If the map is a new world, create a blank map with a blank generator
        if(isNewWorld) {
            worldCreator.generator(new VoidGenerator());
        }

        this.world = worldCreator.createWorld();

        if(isNewWorld) {
            Location spawnBlockLocation = new Location(world, 0, 99, 0);

            world.getChunkAt(spawnBlockLocation).load();
            world.getBlockAt(spawnBlockLocation).setType(Material.STONE);
        }
    }

    /**
     * Teleport all players in the map to spawn and delete the map
     */
    public void destroy(boolean saveWorld) {
        if(!isCreated())
            return;

        for(Player player : world.getPlayers())
            player.teleport(Bukkit.getWorld("world").getSpawnLocation());

        Bukkit.getServer().unloadWorld(world, saveWorld);
    }

    public boolean isCreated() {
        return this.world != null;
    }

    public String getName() {
        return this.world.getName();
    }

    public FootMap getParentMap() {
        return parentMap;
    }

    public File getMapDirectory() {
        return world.getWorldFolder();
    }

    public World getWorld() {
        return world;
    }

    public Location getSpawnLocation() {
        return new Location(world, 0, 100, 0);
    }

}
