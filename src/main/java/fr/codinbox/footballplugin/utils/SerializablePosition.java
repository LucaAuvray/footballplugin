package fr.codinbox.footballplugin.utils;

import org.bukkit.Location;
import org.bukkit.World;

public class SerializablePosition {

    private double x, y, z;
    private float pitch, yaw;

    public SerializablePosition(double x, double y, double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public SerializablePosition(Location location) {
        this(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    public SerializablePosition() {
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

}
