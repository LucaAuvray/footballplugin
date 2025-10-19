package fr.codinbox.footballplugin.service;

import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.map.MinecraftWorld;
import fr.codinbox.footballplugin.mode.FootMode;
import org.bukkit.World;

import java.util.Iterator;

public interface MinecraftWorldService extends PluginService {

    /**
     * Create a {@link FootMap} and assign him a name
     * @param name Name of the map
     * @return Fresh created {@link FootMap}
     */
    FootMap createMap(String name);

    /**
     * Delete a {@link FootMap}
     * @param map The map to delete
     */
    void deleteMap(FootMap map);

    /**
     * Get a {@link FootMap} by his name
     * @param name Name of the map
     * @return Corresponding {@link FootMap}
     */
    FootMap getMapByName(String name);

    /**
     * Get all the maps
     * @return {@link Iterator} of {@link FootMap}
     */
    Iterator<FootMap> getMaps();

    /**
     * Get all the loaded {@link FootMap} with a precise {@link FootMode}
     * @param mode Mode to search
     * @return {@link Iterator} of {@link FootMap}
     */
    Iterator<FootMap> getMaps(FootMode mode);

    /**
     * Save all the maps into a data file
     */
    void saveMaps();

    /**
     * Create a {@link MinecraftWorld} with a precise {@link FootMap}
     * @param map The map to copy
     * @return Fresh created {@link MinecraftWorld}
     */
    MinecraftWorld loadWorld(FootMap map, boolean editMode);

    /**
     * Unload a {@link MinecraftWorld} to free memory and save it if the world is in edition
     * @param world {@link MinecraftWorld} world to unload
     */
    void unloadWorld(MinecraftWorld world);

    /**
     * Get all listed {@link MinecraftWorld}
     * @return {@link Iterator<MinecraftWorld>} of {@link MinecraftWorld}
     */
    Iterator<MinecraftWorld> getWorlds();

    /**
     * Get listed {@link MinecraftWorld} with a precise {@link FootMode}
     * @param mode Mode to search
     * @return {@link Iterator} of {@link MinecraftWorld}
     */
    Iterator<MinecraftWorld> getWorlds(FootMode mode);

    /**
     * Get listed {@link MinecraftWorld} with a precide {@link FootMap}
     * @param map Map to search
     * @return {@link Iterator} of {@link MinecraftWorld}
     */
    Iterator<MinecraftWorld> getWorlds(FootMap map);

    /**
     * Get the {@link MinecraftWorld} that is currently in edition
     * @param mode Mode in edition
     * @return {@link MinecraftWorld} if a world of this mode is in edition, or NULL if the world does not exist
     */
    MinecraftWorld getEditingWorld(FootMode mode);

    /**
     * Get a {@link MinecraftWorld} by its {@link World}
     * @param world Bukkit world to search
     * @return Corresponding {@link MinecraftWorld}
     */
    MinecraftWorld getWorldByBukkitWorld(World world);

}
