package fr.codinbox.footballplugin.mode;

import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.player.FootStat;
import fr.codinbox.footballplugin.team.FootTeamQualifier;

import java.util.ArrayList;
import java.util.HashMap;

public enum FootMode {

    CASUAL__TWO_VERSUS_TWO(true, 2, "CASUAL", "TWO_VERSUS_TWO", "CASUAL_TWOS"),
    CASUAL__THREE_VERSUS_THREE(true, 3, "CASUAL", "THREE_VERSUS_THREE", "CASUAL_THREES"),
    RANKED__TWO_VERSUS_TWO(true, 2, "RANKED", "TWO_VERSUS_TWO", "RANKED_TWOS"),
    RANKED__THREE_VERSUS_THREE(true, 3, "RANKED", "THREE_VERSUS_THREE", "RANKED_THREES"),
    CUSTOM(false, -1, "CUSTOM", null, null);

    private final boolean activateMmr;
    private final int numberOfSpawns;
    private String modeName;
    private String subModeName;
    private final String statPrefix;

    FootMode(boolean activateMmr, int numberOfSpawns, String modeName, String subModeName, String statPrefix) {
        this.activateMmr = activateMmr;
        this.numberOfSpawns = numberOfSpawns;
        this.modeName = modeName;
        this.subModeName = subModeName;
        this.statPrefix = statPrefix;
    }

    public static void initLanguage(LanguageManager languageManager) {
        for(FootMode mode : values()) {
            mode.modeName = languageManager.getPhrase(LanguageKey.toLanguageKey(mode.modeName));
            if(mode.subModeName == null)
                continue;
            mode.subModeName = languageManager.getPhrase(LanguageKey.toLanguageKey(mode.subModeName));
        }
    }

    public static ArrayList<FootMode> getAllModes(boolean isMmr) {
        ArrayList<FootMode> modes = new ArrayList<>();
        for(FootMode footMode : values())
            if(footMode.hasMmrActivated())
                modes.add(footMode);
        return modes;
    }

    public static int getItemPosition(FootMode mode) {
        FootMode[] modes = values();
        for (int i = 0; i < values().length; i++) {
            if(modes[i].equals(mode))
                return i;
        }
        return -1;
    }

    public static int getMinNumberOfPlayer() {
        int min = Integer.MAX_VALUE;
        for (FootMode mode : values()) {
            if(mode.getNumberOfSpawns() <= 0)
                continue;
            int players = mode.getNumberOfSpawns()* FootTeamQualifier.values().length;
            if(players < min)
                min = players;
        }
        return min;
    }

    public boolean hasMmrActivated() {
        return this.activateMmr;
    }

    public int getNumberOfSpawns() {
        return numberOfSpawns;
    }

    public String getModeName() {
        return modeName;
    }

    public String getSubModeName() {
        return subModeName;
    }

    public String getStatPrefix() {
        return statPrefix;
    }

}
