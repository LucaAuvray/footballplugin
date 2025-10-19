package fr.codinbox.footballplugin.listeners;

import fr.codinbox.footballplugin.FootballPlugin;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldListener implements Listener {

    private FootballPlugin plugin;

    public WorldListener(FootballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World world = event.getWorld();
        if(plugin.getWorldService().getWorldByBukkitWorld(world) != null)
            event.setCancelled(event.toWeatherState());
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        World world = event.getLocation().getWorld();
        if(plugin.getWorldService().getWorldByBukkitWorld(world) != null)
            if(!(event.getEntity() instanceof Item))
                event.setCancelled(true);
    }

}
