package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.matchmaking.MatchmakingCriteria;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.ComparablePlayerList;
import fr.codinbox.footballplugin.player.FootPlayer;
import fr.codinbox.footballplugin.ranking.Rank;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class RankedPlaylistFiller implements IFiller {

    public static final String TWO_VERSUS_TWO_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2EyOGExMzUzODMzZjQwYTQyMWNhYmFhMzk2NWI5NzBhZDlmNjJjMWQ5NjJhY2E5ODQxNGQyZGVjNWMzMzgifX19";
    public static final String THREE_VERSUS_THREE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTUxYzZkYTQ2Zjc1ODViZjJmZDVjYTQ0YzRhYmVjNTY4YzIzZWFmNjdjM2Y2ODFjZDJiYzFiM2ViMjc1YSJ9fX0==";

    @Override
    public void onOpen(InventoryProvider provider, Player player) {
        provider.setItem(2, 4, ClickableItem.on(get2SModeItem(player), onClick -> {
            if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
                player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
                return;
            }

            FootballPlugin.INSTANCE.getMatchmakingService().joinMatchmaking(player, new MatchmakingCriteria(FootMode.RANKED__TWO_VERSUS_TWO, null));
            player.closeInventory();
        }));

        provider.setItem(2, 6, ClickableItem.on(get3SModeItem(player), onClick -> {
            if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
                player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
                return;
            }

            FootballPlugin.INSTANCE.getMatchmakingService().joinMatchmaking(player, new MatchmakingCriteria(FootMode.RANKED__THREE_VERSUS_THREE, null));
            player.closeInventory();
        }));
    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack get2SModeItem(Player player) {
        FootPlayer footPlayer = FootballPlugin.INSTANCE.getPlayerService().getPlayerData(player);
        double mmr = footPlayer.getMmr().get(FootConfig.CURRENT_SEASON).get(FootMode.RANKED__TWO_VERSUS_TWO);
        Rank rank = FootballPlugin.INSTANCE.getRankingService().getRankByMmr(mmr);
        int division = rank.getDivisionNumber(mmr);
        ComparablePlayerList ranking = FootballPlugin.INSTANCE.getPlayerService().getLeaderboard(FootMode.RANKED__TWO_VERSUS_TWO, false);
        return new ItemBuilder(CustomHeads.fromBase64(TWO_VERSUS_TWO_TEXTURE))
                .setName("§b§l" + new LanguageManager.Phrase(LanguageKey.TWO_VERSUS_TWO))
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.RANKED__TWO_VERSUS_TWO) + " §7playing §8(" + FootballPlugin.INSTANCE.getMatchmakingService().getQueuedPlayers(FootMode.RANKED__TWO_VERSUS_TWO).size() + " queued)")
                .addLoreLine("§r")
                .addLoreLine("§7§nCurrent rank:")
                .addLoreLine(rank.getColorCode() + rank.getName() + " Division " + division + " §8(" + new DecimalFormat("##.00").format(mmr) + ")")
                .addLoreLine("§r")
                .addLoreLine("§7§nCurrent ranking:")
                .addLoreLine("§b#" + ranking.getPosition(player) + "/" + ranking.size() + " players")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();
    }

    private ItemStack get3SModeItem(Player player) {
        FootPlayer footPlayer = FootballPlugin.INSTANCE.getPlayerService().getPlayerData(player);
        double mmr = footPlayer.getMmr().get(FootConfig.CURRENT_SEASON).get(FootMode.RANKED__THREE_VERSUS_THREE);
        Rank rank = FootballPlugin.INSTANCE.getRankingService().getRankByMmr(mmr);
        int division = rank.getDivisionNumber(mmr);
        ComparablePlayerList ranking = FootballPlugin.INSTANCE.getPlayerService().getLeaderboard(FootMode.RANKED__THREE_VERSUS_THREE, false);
        return new ItemBuilder(CustomHeads.fromBase64(THREE_VERSUS_THREE_TEXTURE))
                .setName("§b§l" + new LanguageManager.Phrase(LanguageKey.THREE_VERSUS_THREE))
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.RANKED__THREE_VERSUS_THREE) + " §7playing §8(" + FootballPlugin.INSTANCE.getMatchmakingService().getQueuedPlayers(FootMode.RANKED__THREE_VERSUS_THREE).size() + " queued)")
                .addLoreLine("§r")
                .addLoreLine("§7§nCurrent rank:")
                .addLoreLine(rank.getColorCode() + rank.getName() + " Division " + division + " §8(" + new DecimalFormat("##.00").format(mmr) + ")")
                .addLoreLine("§r")
                .addLoreLine("§7§nCurrent ranking:")
                .addLoreLine("§b#" + ranking.getPosition(player) + "/" + ranking.size() + " players")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();
    }

}
