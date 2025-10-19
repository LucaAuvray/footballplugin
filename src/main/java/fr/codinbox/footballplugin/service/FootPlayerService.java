package fr.codinbox.footballplugin.service;

import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.ComparablePlayer;
import fr.codinbox.footballplugin.player.ComparablePlayerList;
import fr.codinbox.footballplugin.player.FootPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public interface FootPlayerService extends PluginService {

    FootPlayer createPlayerData(Player newPlayer);
    FootPlayer getPlayerData(UUID player);
    FootPlayer getPlayerData(OfflinePlayer player);

    ComparablePlayerList getLeaderboard(FootMode mode, boolean recalculate);

    boolean hasData(Player player);

    void saveData(FootPlayer player);

    void saveAllDatas(boolean force);

    void markAsSaving(Player player);

}
