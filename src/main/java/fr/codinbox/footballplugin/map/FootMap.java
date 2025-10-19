package fr.codinbox.footballplugin.map;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.SerializablePosition;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class FootMap {

    /**
     * Name of the map
     */
    private String name;

    /**
     * Mode of the map
     */
    private FootMode mode;

    /**
     * Team spawns
     */
    private HashMap<FootTeamQualifier, ArrayList<SerializablePosition>> teamSpawns;

    /**
     * Center of the map location
     * Also location of the balloon spawn
     */
    private SerializablePosition centerLocation;

    public FootMap(String name, FootMode mode, HashMap<FootTeamQualifier, ArrayList<SerializablePosition>> teamSpawns) {
        this.name = name;
        this.mode = mode;
        this.teamSpawns = teamSpawns;
    }

    public boolean hasEnoughSpawns(FootTeamQualifier team) {
        return this.mode.getNumberOfSpawns() == this.teamSpawns.get(team).size();
    }

    public boolean hasEnoughSpawns() {
        for(FootTeamQualifier team : this.teamSpawns.keySet())
            if(!hasEnoughSpawns(team))
                return false;
        return true;
    }

    public void resetSpawns() {
        for(FootTeamQualifier team : this.teamSpawns.keySet())
            this.teamSpawns.get(team).clear();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMode(FootMode mode) {
        this.mode = mode;
        resetSpawns();
    }

    public void setTeamSpawns(HashMap<FootTeamQualifier, ArrayList<SerializablePosition>> teamSpawns) {
        this.teamSpawns = teamSpawns;
    }

    public void setTeamSpawn(FootTeamQualifier team, int spawnNumber, SerializablePosition location) {
        this.teamSpawns.get(team).set(spawnNumber, location);
    }

    public void addTeamSpawn(FootTeamQualifier team, SerializablePosition location) {
        if(hasEnoughSpawns(team))
            return;

        this.teamSpawns.get(team).add(location);
    }

    public void deleteTeamSpawn(FootTeamQualifier team, SerializablePosition location) {
        this.teamSpawns.get(team).remove(location);
    }

    public SerializablePosition getTeamSpawn(FootTeamQualifier team, int spawn) {
        return this.teamSpawns.get(team).get(spawn);
    }

    public String getName() {
        return name;
    }

    public FootMode getMode() {
        return mode;
    }

    public HashMap<FootTeamQualifier, ArrayList<SerializablePosition>> getTeamSpawns() {
        return teamSpawns;
    }

    public File getSaveDirectory() {
        return new File(FootballPlugin.INSTANCE.getWorldsDirectory(), this.name.toLowerCase(Locale.ROOT));
    }

    public SerializablePosition getCenterSerializableLocation() {
        return centerLocation;
    }

    public Location getCenterLocation(World world) {
        return this.centerLocation != null ? centerLocation.toLocation(world) : null;
    }

    public void setCenterLocation(SerializablePosition centerLocation) {
        this.centerLocation = centerLocation;
    }

}
