package fr.codinbox.footballplugin.service;

import fr.codinbox.footballplugin.matchmaking.MatchmakingCriteria;
import fr.codinbox.footballplugin.mode.FootMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;

public interface MatchmakingService extends PluginService {

    void joinMatchmaking(Player player, MatchmakingCriteria criteria);
    void leaveMatchmaking(Player player);

    MatchmakingCriteria getCurrentMatchmaking(Player player);
    boolean isInMatchmaking(Player player);

    HashMap<Player, MatchmakingCriteria> getQueuedPlayers(FootMode mode);

    void createQueue(FootMode mode);
    void deleteQueue(FootMode mode);
    void processQueue(FootMode mode);

}
