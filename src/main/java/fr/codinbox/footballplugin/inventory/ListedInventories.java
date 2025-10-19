package fr.codinbox.footballplugin.inventory;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.CustomArena;
import fr.codinbox.footballplugin.inventory.fillers.*;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ListedInventories {

    public static OpenableInventory buildWorldEditorMenu(FootMap map) {
        return new OpenableInventory.Builder(9, new LanguageManager.Phrase(LanguageKey.GUI_EDITOR_TITLE)
                .replaceVar("name", map.getName())
                .toString())
                .setFiller(new WorldEditorFiller(map))
                .build();
    }

    public static OpenableInventory buildSpawnEditorMenu(FootMap map, FootTeamQualifier teamQualifier) {
        return new OpenableInventory.Builder(9*3, new LanguageManager.Phrase(LanguageKey.GUI_LOCATION_EDITOR_TITLE)
                .replaceVar("name", map.getName())
                .replaceVar("team", teamQualifier.getLanguageName())
                .toString())
                .setFiller(new SpawnEditorFiller(map, teamQualifier))
                .build();
    }

    public static OpenableInventory buildFootballMenu() {
        return new OpenableInventory.Builder(9*5, "§c§lFootball")
                .setFiller(new FootballMenuFiller())
                .build();
    }

    public static OpenableInventory buildCasualPlaylistMenu() {
        return new OpenableInventory.Builder(9*3, "§c§lFootball > " + new LanguageManager.Phrase(LanguageKey.CASUAL))
                .setFiller(new CasualPlaylistFiller())
                .build();
    }

    public static OpenableInventory buildRankedPlaylistMenu() {
        return new OpenableInventory.Builder(9*3, "§c§lFootball > " + new LanguageManager.Phrase(LanguageKey.RANKED))
                .setFiller(new RankedPlaylistFiller())
                .build();
    }

    public static OpenableInventory buildCustomListMenu() {
        return new OpenableInventory.Builder(9*6, "§c§lFootball > " + new LanguageManager.Phrase(LanguageKey.CUSTOM) + " > List")
                .setFiller(new CustomListFiller())
                .build();
    }

    public static OpenableInventory buildCustomCreateMenu(Player player) {
        return new OpenableInventory.Builder(9*3, "§c§lFootball > " + new LanguageManager.Phrase(LanguageKey.CUSTOM) + " > New")
                .setFiller(new CreateCustomFiller(FootballPlugin.INSTANCE.getArenaManager().getCriterias(player)))
                .build();
    }

    public static OpenableInventory buildCustomMapMenu(Player player) {
        return new OpenableInventory.Builder(9*6, "§c§lFootball > " + new LanguageManager.Phrase(LanguageKey.CUSTOM) + " > New")
                .setFiller(new CustomMapFiller(FootballPlugin.INSTANCE.getArenaManager().getCriterias(player)))
                .build();
    }

    public static OpenableInventory buildCustomTeamMenu(Player player, CustomArena arena) {
        return new OpenableInventory.Builder(9*3, "§c§lFootball > " + new LanguageManager.Phrase(LanguageKey.CUSTOM) + " > Team")
                .setFiller(new CustomTeamFiller(arena))
                .build();
    }

    public static OpenableInventory buildPlayerStatsMenu(OfflinePlayer target, int season) {
        return new OpenableInventory.Builder(9*4, StringUtils.getOrShort(target.getName() + "'s football stats", 32))
                .setFiller(new PlayerStatsFiller(target, season))
                .build();
    }

    public static OpenableInventory buildPlayerStatsSeasonMenu(OfflinePlayer target) {
        return new OpenableInventory.Builder(9*4, "Season chooser")
                .setFiller(new PlayerStatsSeasonFiller(target))
                .build();
    }

}
