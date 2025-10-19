package fr.codinbox.footballplugin.player;

import fr.codinbox.footballplugin.mode.FootMode;

public enum FootStat {

    /*
    CASUAL 2V2 STATS
     */

    CASUAL_TWOS_PLAYED,
    CASUAL_TWOS_GOALS,
    CASUAL_TWOS_ASSISTS,
    CASUAL_TWOS_VICTORIES,
    CASUAL_TWOS_LOSES,
    CASUAL_TWOS_HIGHEST_GOALS,
    CASUAL_TWOS_HIGHEST_ASSISTS,

    /*
    CASUAL 3V3 STATS
     */

    CASUAL_THREES_PLAYED,
    CASUAL_THREES_GOALS,
    CASUAL_THREES_ASSISTS,
    CASUAL_THREES_VICTORIES,
    CASUAL_THREES_LOSES,
    CASUAL_THREES_HIGHEST_GOALS,
    CASUAL_THREES_HIGHEST_ASSISTS,

    /*
    RANKED 2V2 STATS
     */

    RANKED_TWOS_PLAYED,
    RANKED_TWOS_GOALS,
    RANKED_TWOS_ASSISTS,
    RANKED_TWOS_VICTORIES,
    RANKED_TWOS_LOSES,
    RANKED_TWOS_HIGHEST_GOALS,
    RANKED_TWOS_HIGHEST_ASSISTS,

    /*
    RANKED 3V3 STATS
     */

    RANKED_THREES_PLAYED,
    RANKED_THREES_GOALS,
    RANKED_THREES_ASSISTS,
    RANKED_THREES_VICTORIES,
    RANKED_THREES_LOSES,
    RANKED_THREES_HIGHEST_GOALS,
    RANKED_THREES_HIGHEST_ASSISTS;

    public enum PreciseStat {
        PLAYED,
        GOALS,
        ASSISTS,
        VICTORIES,
        LOSES,
        HIGHEST_GOALS,
        HIGHEST_ASSISTS;
    }

    public static FootStat getStat(FootMode mode, PreciseStat stat) {
        for(FootStat s : values()) {
            if(s.name().equals(mode.getStatPrefix() + "_" + stat.name()))
                return s;
        }
        return null;
    }

}
