package fr.codinbox.footballplugin.game;

import fr.codinbox.footballplugin.map.MinecraftWorld;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.action.PlayerExecutor;
import fr.codinbox.footballplugin.team.FootTeam;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public interface Arena {

    UUID getOwner();
    String getName();
    String getPassword();
    boolean isAllowingSpectators();

    PlayerExecutor getPlayerExecutor();
    void joinPlayer(Player player, boolean spectator);
    void leavePlayer(OfflinePlayer player);
    void setTeam(Player player, FootTeam team);

    FootTeam getTeamByQualifier(FootTeamQualifier qualifier);
    Collection<FootTeam> getTeams();

    ArrayList<OfflinePlayer> getIngamePlayers();
    ArrayList<OfflinePlayer> getSpectators();
    ArrayList<OfflinePlayer> getAllPlayers();

    boolean hasPlayer(OfflinePlayer player);

    FootTeam getPlayerTeam(OfflinePlayer player);

    void setState(GameState state);
    GameState getCurrentState();
    boolean isPlaying();
    boolean isFull();

    MinecraftWorld getMap();
    FootMode getMode();

    void startMatch();

    Ball getBall();
    void centerBall();

    void goal(FootTeam team, Location location);

    void endMatch();

    void destroy();

    HashMap<Player, Location> playerSpawnMap();

    Location teleportPlayerToSpawn(Player player);

}
