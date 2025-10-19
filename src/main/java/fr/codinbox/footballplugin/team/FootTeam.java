package fr.codinbox.footballplugin.team;

import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.utils.SerializablePosition;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class FootTeam {

    /**
     * Currentteam qualifier
     */
    private FootTeamQualifier qualifier;

    /**
     * Players into the team
     */
    private ArrayList<UUID> players;

    /**
     * Current map spawns
     */
    private ArrayList<SerializablePosition> spawns;

    /**
     * Team goals
     */
    private int goals;

    /**
     * Team waiting bossbar
     */
    private BossBar waitingBossBar;

    public FootTeam(FootTeamQualifier qualifier, ArrayList<UUID> players, ArrayList<SerializablePosition> spawns) {
        this.qualifier = qualifier;
        this.players = players;
        this.spawns = spawns;
        this.goals = 0;
        this.waitingBossBar = Bukkit.createBossBar(new LanguageManager.Phrase(LanguageKey.BOSS_TEAM_INFO)
                .replaceVar("teamcolor", qualifier.getColorCode())
                .replaceVar("name", qualifier.getLanguageName())
                .toString(),
                qualifier.getBarColor(),
                BarStyle.SOLID
        );
        waitingBossBar.setProgress(1.0d);
    }

    public ArrayList<UUID> getPlayersUuid() {
        return players;
    }

    public ArrayList<OfflinePlayer> getPlayers() {
        ArrayList<OfflinePlayer> players = new ArrayList<>();
        for (UUID player : this.players) {
            players.add(Bukkit.getOfflinePlayer(player));
        }
        return players;
    }

    public ArrayList<SerializablePosition> getSpawns() {
        return spawns;
    }

    public boolean hasPlayer(UUID player) {
        return this.players.contains(player);
    }

    public FootTeamQualifier getQualifier() {
        return qualifier;
    }

    public int getGoals() {
        return goals;
    }

    public void setGoals(int goals) {
        this.goals = goals;
    }

    public boolean isFull() {
        return players.size() >= spawns.size();
    }

    public BossBar getWaitingBossBar() {
        return waitingBossBar;
    }

}
