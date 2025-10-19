package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.matchmaking.MatchmakingCriteria;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CasualPlaylistFiller implements IFiller {

    public static final String TWO_VERSUS_TWO_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFkOTlmN2RmNmRkMzE1OTdkYzc0MTg5MzU2NmJmN2NhYzdlZGUzODM0Nzk2NWQ2MGFlYWE0ZGRjZjJlZSJ9fX0=";
    public static final String THREE_VERSUS_THREE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2JlNTVkMjg0MmQ2ZDkyNDkwMWZkN2I0ODM1YjY0ODczNzJlN2FkNjY5NDdjNmExZDQ0OGFiMmRiOGM2NzBlYyJ9fX0==";

    @Override
    public void onOpen(InventoryProvider provider, Player player) {
        provider.setItem(2, 4, ClickableItem.on(get2SModeItem(), onClick -> {
            if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
                player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
                return;
            }

            FootballPlugin.INSTANCE.getMatchmakingService().joinMatchmaking(player, new MatchmakingCriteria(FootMode.CASUAL__TWO_VERSUS_TWO, null));
            player.closeInventory();
        }));

        provider.setItem(2, 6, ClickableItem.on(get3SModeItem(), onClick -> {
            if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
                player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
                return;
            }

            FootballPlugin.INSTANCE.getMatchmakingService().joinMatchmaking(player, new MatchmakingCriteria(FootMode.CASUAL__THREE_VERSUS_THREE, null));
            player.closeInventory();
        }));
    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack get2SModeItem() {
        return new ItemBuilder(CustomHeads.fromBase64(TWO_VERSUS_TWO_TEXTURE))
                .setName("§b§l" + new LanguageManager.Phrase(LanguageKey.TWO_VERSUS_TWO))
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.CASUAL__TWO_VERSUS_TWO) + " §7playing §8(" + FootballPlugin.INSTANCE.getMatchmakingService().getQueuedPlayers(FootMode.CASUAL__TWO_VERSUS_TWO).size() + " queued)")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();
    }

    private ItemStack get3SModeItem() {
        return new ItemBuilder(CustomHeads.fromBase64(THREE_VERSUS_THREE_TEXTURE))
                .setName("§b§l" + new LanguageManager.Phrase(LanguageKey.THREE_VERSUS_THREE))
                .addLoreLine("§7➤ §a" + FootballPlugin.INSTANCE.getArenaManager().getNumberOfPlayingPlayers(FootMode.CASUAL__THREE_VERSUS_THREE) + " §7playing §8(" + FootballPlugin.INSTANCE.getMatchmakingService().getQueuedPlayers(FootMode.CASUAL__THREE_VERSUS_THREE).size() + " queued)")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to play!")
                .toItemStack();
    }

}
