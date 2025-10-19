package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.inventory.ListedInventories;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.FootPlayer;
import fr.codinbox.footballplugin.player.FootStat;
import fr.codinbox.footballplugin.ranking.Rank;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class PlayerStatsFiller implements IFiller {

    private OfflinePlayer playerStats;
    private int season;

    public PlayerStatsFiller(OfflinePlayer playerStats, int season) {
        this.playerStats = playerStats;
        this.season = season;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {

        ItemStack headItem = FootballMenuFiller.getPlayerProfileItem(playerStats, false);

        provider.setItem(2, 2, ClickableItem.empty(headItem));
        provider.setItem(3, 2, ClickableItem.on(getSeasonChooseButton(), onClick -> {
            FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildPlayerStatsSeasonMenu(playerStats), player);
        }));

        provider.setItem(2, 4, ClickableItem.empty(getCasual2sStatsSign()));
        provider.setItem(3, 4, ClickableItem.empty(getStatsPaper(FootMode.CASUAL__TWO_VERSUS_TWO)));

        provider.setItem(2, 5, ClickableItem.empty(getCasual3sStatsSign()));
        provider.setItem(3, 5, ClickableItem.empty(getStatsPaper(FootMode.CASUAL__THREE_VERSUS_THREE)));

        provider.setItem(2, 7, ClickableItem.empty(getRanked2sStatsSign()));
        provider.setItem(3, 7, ClickableItem.empty(getStatsPaper(FootMode.RANKED__TWO_VERSUS_TWO)));

        provider.setItem(2, 8, ClickableItem.empty(getRanked3sStatsSign()));
        provider.setItem(3, 8, ClickableItem.empty(getStatsPaper(FootMode.RANKED__THREE_VERSUS_THREE)));

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getCasual2sStatsSign() {
        return new ItemBuilder(CustomHeads.fromBase64(CasualPlaylistFiller.TWO_VERSUS_TWO_TEXTURE))
                .setName("§b" + new LanguageManager.Phrase(LanguageKey.CASUAL) + " " + new LanguageManager.Phrase(LanguageKey.TWO_VERSUS_TWO))
                .toItemStack();
    }

    private ItemStack getCasual3sStatsSign() {
        return new ItemBuilder(CustomHeads.fromBase64(CasualPlaylistFiller.THREE_VERSUS_THREE_TEXTURE))
                .setName("§b" + new LanguageManager.Phrase(LanguageKey.CASUAL) + " " + new LanguageManager.Phrase(LanguageKey.THREE_VERSUS_THREE))
                .toItemStack();
    }

    private ItemStack getRanked2sStatsSign() {
        return new ItemBuilder(CustomHeads.fromBase64(RankedPlaylistFiller.TWO_VERSUS_TWO_TEXTURE))
                .setName("§b" + new LanguageManager.Phrase(LanguageKey.RANKED) + " " + new LanguageManager.Phrase(LanguageKey.TWO_VERSUS_TWO))
                .toItemStack();
    }

    private ItemStack getRanked3sStatsSign() {
        return new ItemBuilder(CustomHeads.fromBase64(RankedPlaylistFiller.THREE_VERSUS_THREE_TEXTURE))
                .setName("§b" + new LanguageManager.Phrase(LanguageKey.RANKED) + " " + new LanguageManager.Phrase(LanguageKey.THREE_VERSUS_THREE))
                .toItemStack();
    }

    private ItemStack getStatsPaper(FootMode mode) {

        FootPlayer player = FootballPlugin.INSTANCE.getPlayerService().getPlayerData(playerStats.getUniqueId());
        Rank rank = FootballPlugin.INSTANCE.getRankingService().getRankByMmr(player.getMmr().get(season).get(mode));

        ItemBuilder builder = new ItemBuilder(Material.PAPER);

        builder.setName("§b► " + mode.getModeName() + " " + mode.getSubModeName());

        Double victories = player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.VICTORIES));
        Double loses = player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.LOSES));
        Double wr;
        if(loses != 0)
            wr = victories / loses;
        else
            wr = victories;

        builder.addLoreLine("§r")
                .addLoreLine("§8► §7Current rank:")
                .addLoreLine("   " + rank.getColorCode() + rank.getName() + " Division " + rank.getDivisionNumber(player.getMmr().get(season).get(mode)))
                .addLoreLine("   §a#" + FootballPlugin.INSTANCE.getRankingService().getRankings(mode, false).getPosition(playerStats) + "/" + FootballPlugin.INSTANCE.getRankingService().getRankings(mode, false).size())
                .addLoreLine("§r")
                .addLoreLine("§8► §7Played matches: §9" + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.PLAYED))))
                .addLoreLine("  §7Wins: §9" + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.VICTORIES))))
                .addLoreLine("  §7Loses: §9" + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.LOSES))))
                .addLoreLine("  §7Ratio: §9" + new DecimalFormat("#0.00").format(wr))
                .addLoreLine("§r")
                .addLoreLine("§8► §7Total goals: §9"
                        + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.GOALS)))
                        + " §8(Highest: "
                        + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.HIGHEST_GOALS)))
                        + ")")
                .addLoreLine("§8► §7Total assists: §9"
                        + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.ASSISTS)))
                        + " §8(Highest: "
                        + Math.round(player.getStats().get(season).get(FootStat.getStat(mode, FootStat.PreciseStat.ASSISTS)))
                        + ")");

        return builder.toItemStack();

    }

    private ItemStack getSeasonChooseButton() {
        return new ItemBuilder(Material.STONE_BUTTON)
                .setName("§9§lSeason " + season)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to change season!")
                .toItemStack();
    }

}
