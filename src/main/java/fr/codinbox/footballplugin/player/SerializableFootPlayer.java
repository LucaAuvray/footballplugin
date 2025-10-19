package fr.codinbox.footballplugin.player;

import fr.codinbox.footballplugin.mode.FootMode;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.UUID;

public class SerializableFootPlayer {

    private UUID uuid;
    private HashMap<Integer, HashMap<FootMode, Double>> mmr;
    private HashMap<Integer, HashMap<FootStat, Double>> stats;
    private Long joinDate;

    public SerializableFootPlayer(UUID uuid, HashMap<Integer, HashMap<FootMode, Double>> mmr, HashMap<Integer, HashMap<FootStat, Double>> stats, Long joinDate) {
        this.uuid = uuid;
        this.mmr = mmr;
        this.stats = stats;
        this.joinDate = joinDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public HashMap<Integer, HashMap<FootMode, Double>> getMmr() {
        return mmr;
    }

    public HashMap<Integer, HashMap<FootStat, Double>> getStats() {
        return stats;
    }

    public Long getJoinDate() {
        return joinDate;
    }

    public FootPlayer toLegacyFootPlayer() {
        return new FootPlayer(Bukkit.getOfflinePlayer(uuid), mmr, stats, joinDate, true);
    }

}
