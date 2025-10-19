package fr.codinbox.footballplugin.player;

import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.mode.FootMode;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;

public class FootPlayer {

    /**
     * Corresponding player
     */
    private final OfflinePlayer player;

    /**
     * Player mmr in all modes (excluding non-mmr modes)
     */
    private final HashMap<Integer, HashMap<FootMode, Double>> mmr;

    /**
     * Player statistics
     */
    private HashMap<Integer, HashMap<FootStat, Double>> stats;

    /**
     * Player join date
     */
    private Long joinDate;

    public FootPlayer(OfflinePlayer player, HashMap<Integer, HashMap<FootMode, Double>> mmr, HashMap<Integer, HashMap<FootStat, Double>> stats, Long joinDate, boolean check) {
        this.player = player;
        this.mmr = mmr;
        this.stats = stats;
        this.joinDate = joinDate;
        if(!check)
            return;
        FootPlayer newPlayer = newPlayer(player);
        for(Integer season : newPlayer.getMmr().keySet()) {
            if(!mmr.containsKey(FootConfig.CURRENT_SEASON))
                if(mmr.isEmpty())
                    mmr.put(season, newPlayer.getMmr().get(FootConfig.CURRENT_SEASON));
                else {
                    int lastSeason = Integer.MIN_VALUE;
                    for(int i : mmr.keySet())
                        lastSeason = Math.max(i, lastSeason);
                    mmr.put(season, mmr.get(lastSeason));
                }
        }
        if(!stats.containsKey(FootConfig.CURRENT_SEASON))
            stats.put(FootConfig.CURRENT_SEASON, new HashMap<>());
        for(FootStat stat : newPlayer.getStats().get(FootConfig.CURRENT_SEASON).keySet()) {
            if(!stats.get(FootConfig.CURRENT_SEASON).containsKey(stat)) {
                stats.get(FootConfig.CURRENT_SEASON).put(stat, newPlayer.getStats().get(FootConfig.CURRENT_SEASON).get(stat));
            }
        }
    }

    public FootPlayer() {
        this(null, null, null, null, false);
    }

    public static FootPlayer newPlayer(OfflinePlayer player) {
        // Default mmr map
        HashMap<Integer, HashMap<FootMode, Double>> defaultMmr = new HashMap<Integer, HashMap<FootMode, Double>>() {{
            put(FootConfig.CURRENT_SEASON, new HashMap<>());
            FootMode.getAllModes(true).forEach(mode -> get(FootConfig.CURRENT_SEASON).put(mode, FootConfig.RANKING_MMR_STARTING_MMR));
        }};

        // Default stats map
        HashMap<Integer, HashMap<FootStat, Double>> defaultStats = new HashMap<Integer, HashMap<FootStat, Double>>() {{
            put(FootConfig.CURRENT_SEASON, new HashMap<>());
        }};

        for (FootStat stat : FootStat.values()) {
            defaultStats.get(FootConfig.CURRENT_SEASON).put(stat, 0D);
        }

        // Current time
        long currentTime = System.currentTimeMillis();

        return new FootPlayer(player, defaultMmr, defaultStats, currentTime, false);
    }

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    public HashMap<Integer, HashMap<FootMode, Double>> getMmr() {
        return mmr;
    }

    public HashMap<Integer, HashMap<FootStat, Double>> getStats() {
        return stats;
    }

    public HashMap<FootStat, Double> getCurrentSeasonStats() {
        return stats.get(FootConfig.CURRENT_SEASON);
    }

    public Long getJoinDate() {
        return joinDate;
    }

    public SerializableFootPlayer toSerializablePlayer() {
        return new SerializableFootPlayer(player.getUniqueId(), mmr, stats, joinDate);
    }

}
