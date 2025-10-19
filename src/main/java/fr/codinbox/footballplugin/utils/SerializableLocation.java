package fr.codinbox.footballplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SerializableLocation {

    private SerializablePosition position;
    private String world;

    public SerializableLocation(SerializablePosition position, String world) {
        this.position = position;
        this.world = world;
    }

    public SerializableLocation(Location location) {
        this.position = new SerializablePosition(location);
        this.world = location.getWorld().getName();
    }

    public SerializablePosition getPosition() {
        return position;
    }

    public String getWorld() {
        return world;
    }

    public Location toLocation() {
        return position.toLocation(Bukkit.getWorld(this.world));
    }

}
