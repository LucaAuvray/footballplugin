package fr.codinbox.footballplugin.game;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.team.FootTeam;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GameBossBar {

    private Arena arena;

    private GameState lastUpdateState;
    private HashMap<FootTeamQualifier, BossBar> teamsBossBar;

    public GameBossBar(Arena arena) {
        this.arena = arena;
        this.teamsBossBar = new HashMap<FootTeamQualifier, BossBar>() {{

            for(FootTeamQualifier qualifier : FootTeamQualifier.values()) {
                if (GameState.WAITING.equals(arena.getCurrentState())) {
                    if(lastUpdateState != GameState.WAITING)
                        lastUpdateState = GameState.WAITING;

                    BossBar teamBossBar = createWaitingBossBar(qualifier);

                    put(qualifier, teamBossBar);
                }
            }

        }};
    }

    private BossBar createWaitingBossBar(FootTeamQualifier qualifier) {
        BossBar teamBossBar = Bukkit.createBossBar(new NamespacedKey(FootballPlugin.INSTANCE, arena.getMap().getName() + "-" + qualifier.getLanguageName()),
                getCurrentBossBarContent(qualifier),
                qualifier.getBarColor(),
                BarStyle.SOLID
        );
        teamBossBar.setProgress(1.0d);
        return teamBossBar;
    }

    private boolean deleteBossBar(BossBar bar) {
        FootTeamQualifier teamBossBar = getTeamByBossBar(bar);

        if (teamBossBar == null)
            return false;

        NamespacedKey namespacedKey = new NamespacedKey(FootballPlugin.INSTANCE, arena.getMap().getName() + "-" + teamBossBar.getLanguageName());

        bar.removeAll();
        return Bukkit.removeBossBar(namespacedKey);
    }

    private FootTeamQualifier getTeamByBossBar(BossBar bar) {
        for(Map.Entry<FootTeamQualifier, BossBar> entry : teamsBossBar.entrySet())
            if(entry.getValue().equals(bar))
                return entry.getKey();
        return null;
    }

    private String getCurrentBossBarContent(FootTeamQualifier qualifier) {
        if(GameState.WAITING.equals(arena.getCurrentState()) || GameState.STARTING.equals(arena.getCurrentState())) {
            return new LanguageManager.Phrase(LanguageKey.BOSS_TEAM_INFO)
                    .replaceVar("teamcolor", qualifier.getColorCode())
                    .replaceVar("name", qualifier.getLanguageName())
                    .toString();
        }
        else {
            FootTeamQualifier otherTeamQualifier = qualifier.getOtherTeam();

            return new LanguageManager.Phrase(LanguageKey.BOSS_GOALS)
                    .replaceVar("currentteamcolor", qualifier.getColorCode())
                    .replaceVar("currentteamname", qualifier.getLanguageName())
                    .replaceVar("currentteamgoals", String.valueOf(arena.getTeamByQualifier(qualifier).getGoals()))
                    .replaceVar("enemyteamcolor", otherTeamQualifier.getColorCode())
                    .replaceVar("enemyteamname", otherTeamQualifier.getLanguageName())
                    .replaceVar("enemyteamgoals", String.valueOf(arena.getTeamByQualifier(otherTeamQualifier).getGoals()))
                    .toString();
        }
    }

    public void createBossBar(Player player) {
        FootTeam playerTeam = arena.getPlayerTeam(player);

        if(playerTeam == null)
            playerTeam = arena.getTeamByQualifier(FootTeamQualifier.BLUE);

        FootTeamQualifier playerTeamQualifier = playerTeam.getQualifier();

        teamsBossBar.get(playerTeamQualifier).addPlayer(player);
    }

    public void updateBossBars() {
        for(FootTeamQualifier qualifier : teamsBossBar.keySet()) {
            BossBar bossBar = teamsBossBar.get(qualifier);
            bossBar.setTitle(getCurrentBossBarContent(qualifier));
        }
    }

    public void removeBossBar(Player player) {
        FootTeam playerTeam = arena.getPlayerTeam(player);

        if(playerTeam == null)
            playerTeam = arena.getTeamByQualifier(FootTeamQualifier.BLUE);

        FootTeamQualifier playerTeamQualifier = playerTeam.getQualifier();

        if(!teamsBossBar.containsKey(playerTeamQualifier) || teamsBossBar.get(playerTeamQualifier) == null)
            return;

        BossBar bossBar = teamsBossBar.get(playerTeamQualifier);
        bossBar.removePlayer(player);
    }

    public void destroy() {
        for(BossBar bar : teamsBossBar.values())
            deleteBossBar(bar);
    }

}
