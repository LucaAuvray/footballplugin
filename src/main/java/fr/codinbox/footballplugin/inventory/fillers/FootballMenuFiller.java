package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.inventory.ListedInventories;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.matchmaking.MatchmakingCriteria;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.FootPlayer;
import fr.codinbox.footballplugin.player.FootStat;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class FootballMenuFiller implements IFiller {

    private static final String CASUAL_ITEM_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjJlNTk0ZWExNTQ4NmViMTkyNjFmMjExMWU5NTgzN2FkNmU5YTZiMWQ1NDljNzBlY2ZlN2Y4M2U0MTM2MmI1NyJ9fX0=";
    private static final String RANKED_ITEM_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU4MjdmNDVhYWU2NTY4MWJiMjdlM2UwNDY1YWY2MjI4ZWQ2MjkyYmI2M2IwYTc3NjQ1OTYyMjQ3MjdmOGQ4MSJ9fX0=";

    @Override
    public void onOpen(InventoryProvider provider, Player player) {
        provider.setItem(1, 5, ClickableItem.on(getCancelSearchItem(player), onClick -> {
            FootballPlugin.INSTANCE.getMatchmakingService().leaveMatchmaking(player);
            player.closeInventory();
        }));

        provider.setItem(2, 2, ClickableItem.on(getCasualGameItem(), onClick -> {
            FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildCasualPlaylistMenu(), player);
        }));

        provider.setItem(2, 5, ClickableItem.on(getRankedGameItem(), onClick -> {
            FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildRankedPlaylistMenu(), player);
        }));

        if(FootballPlugin.INSTANCE.getWorldService().getMaps(FootMode.CUSTOM).hasNext())
            provider.setItem(2, 8, ClickableItem.on(getCustomGameItem(), onClick -> {
                FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildCustomListMenu(), player);
            }));

        provider.setItem(4, 5, ClickableItem.on(getPlayerProfileItem(player, true), onClick -> {
            FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildPlayerStatsMenu(player, FootConfig.CURRENT_SEASON), player);
        }));

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getCasualGameItem() {
        return new ItemBuilder(CustomHeads.fromBase64(CASUAL_ITEM_TEXTURE))
                .setName("§bPlay " + new LanguageManager.Phrase(LanguageKey.CASUAL).toString() + " mode")
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.CASUAL__TWO_VERSUS_TWO, FootMode.CASUAL__THREE_VERSUS_THREE) + " §7playing")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();

    }

    private ItemStack getRankedGameItem() {
        return new ItemBuilder(CustomHeads.fromBase64(RANKED_ITEM_TEXTURE))
                .setName("§bPlay " + new LanguageManager.Phrase(LanguageKey.RANKED).toString() + " mode")
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.RANKED__TWO_VERSUS_TWO, FootMode.RANKED__THREE_VERSUS_THREE) + " §7playing")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();
    }

    private ItemStack getCustomGameItem() {
        return new ItemBuilder(Material.ANVIL)
                .setName("§bPlay " + new LanguageManager.Phrase(LanguageKey.CUSTOM).toString() + " mode")
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.CUSTOM) + " §7playing")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();
    }

    public static ItemStack getPlayerProfileItem(OfflinePlayer player, boolean canClick) {
        FootPlayer footPlayer = FootballPlugin.INSTANCE.getPlayerService().getPlayerData(player.getUniqueId());

        ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD, 1, (byte) 3)
                .setSkullOwner(player.getName())
                .setName("§d" + ChatColor.stripColor(player.getName()) + "'s profile");

        double playedMatches = 0;
        double wins = 0;
        double loses = 0;
        double goals = 0;
        double assists = 0;
        double highestGoals = 0;
        double highestAssists = 0;
        for(FootMode mode : FootMode.values()) {
            if(FootMode.CUSTOM.equals(mode))
                continue;

            playedMatches += footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.PLAYED));
            wins += footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.VICTORIES));
            loses += footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.LOSES));
            goals += footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.GOALS));
            assists += footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.ASSISTS));
            highestGoals = Math.max(footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.HIGHEST_GOALS)), highestGoals);
            highestAssists = Math.max(footPlayer.getCurrentSeasonStats().get(FootStat.getStat(mode, FootStat.PreciseStat.HIGHEST_ASSISTS)), highestAssists);
        }

        double ratio = (loses != 0) ? wins/loses : wins;

        builder.addLoreLine("§r")
                .addLoreLine("§a§lSEASON " + FootConfig.CURRENT_SEASON)
                .addLoreLine("§r")
                .addLoreLine("§8► §7Played matches: §9" + Math.round(playedMatches))
                .addLoreLine("  §7Wins: §9" + Math.round(wins))
                .addLoreLine("  §7Loses: §9" + Math.round(loses))
                .addLoreLine("  §7Ratio: §9" + new DecimalFormat("#0.00").format(ratio))
                .addLoreLine("§r")
                .addLoreLine("§8► §7Total goals: §9"
                        + Math.round(goals)
                        + " §8(Highest: " + Math.round(highestGoals)
                        + ")")
                .addLoreLine("§8► §7Total assists: §9"
                        + Math.round(assists)
                        + " §8(Highest: "
                        + Math.round(highestAssists)
                        + ")");

        if(canClick) {
                builder.addLoreLine("§r")
                    .addLoreLine("§eLeft-click to consult your profile!");
        }

        return builder.toItemStack();
    }

    private ItemStack getCancelSearchItem(Player player) {
        MatchmakingCriteria criteria = FootballPlugin.INSTANCE.getMatchmakingService().getCurrentMatchmaking(player);
        if(criteria == null)
            return new ItemStack(Material.AIR);
        return new ItemBuilder(Material.BARRIER)
                .setName("§7Currently searching for: §b§l" + criteria.getMode().getModeName() + " " + (criteria.getMode().getSubModeName() != null ? criteria.getMode().getSubModeName() : ""))
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(criteria.getMode()) + " §7playing §8(" + FootballPlugin.INSTANCE.getMatchmakingService().getQueuedPlayers(criteria.getMode()).size() + " queued)")
                .addLoreLine("§r")
                .addLoreLine("§cLeft-click to cancel")
                .toItemStack();
    }

}
