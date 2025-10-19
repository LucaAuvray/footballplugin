package fr.codinbox.footballplugin.game;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.mode.FootMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ArenaManager {

    private ArrayList<Arena> arenas;
    private HashMap<UUID, Arena> playerArenas;
    private HashMap<Player, CustomGameCriterias> playerCriterias;

    public ArenaManager() {
        this.arenas = new ArrayList<>();
        this.playerArenas = new HashMap<>();
        this.playerCriterias = new HashMap<>();
    }

    protected void registerArena(Arena arena) {
        this.arenas.add(arena);
    }
    protected void unregisterArena(Arena arena) {
        this.arenas.remove(arena);
    }

    protected void reportPlayerArena(Player player, Arena arena) {
        this.playerArenas.put(player.getUniqueId(), arena);
    }

    protected void reportPlayerLeaveArena(OfflinePlayer player) {
        this.playerArenas.remove(player.getUniqueId());
    }

    public CustomGameCriterias registerNewCriterias(Player player) {
        CustomGameCriterias criterias = new CustomGameCriterias(FootballPlugin.INSTANCE.getWorldService().getMaps(FootMode.CUSTOM).next(), true, false, "");
        this.playerCriterias.put(player, criterias);
        return criterias;
    }

    public CustomGameCriterias getCriterias(Player player) {
        return this.playerCriterias.get(player);
    }

    public int getNumberOfPlayingPlayers(FootMode mode) {
        int playing = 0;
        for(Arena arena : arenas) {
            if(mode.equals(arena.getMode())) {
                if (arena.isPlaying())
                    playing += arena.getIngamePlayers().size();
                else
                    playing += arena.getAllPlayers().size();
            }
        }
        return playing;
    }

    public int getNumberOfPlayingPlayers(FootMode... modes) {
        if(modes == null)
            return 0;
        int playing = 0;
        for(FootMode mode : modes)
            playing += getNumberOfPlayingPlayers(mode);
        return playing;
    }

    public void exit() {
        for (Arena arena : new ArrayList<>(arenas))
            arena.destroy();
    }

    public Arena getCurrentArena(Player player) {
        return this.playerArenas.get(player.getUniqueId());
    }

    public boolean isInGame(Player player) {
        return getCurrentArena(player) != null;
    }

    public ArrayList<Arena> getArenas() {
        return arenas;
    }

    public ArrayList<Arena> getArenasByMode(FootMode mode) {
        ArrayList<Arena> arenas = new ArrayList<>();
        getArenas().forEach(arena -> { if(mode.equals(arena.getMode())) arenas.add(arena); });
        return arenas;
    }

    public int getNumberOfPlayingPlayers() {
        return this.playerArenas.size();
    }

}
