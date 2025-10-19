package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.inventory.*;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.player.FootPlayer;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerStatsSeasonFiller implements IFiller {

    private OfflinePlayer target;

    public PlayerStatsSeasonFiller(OfflinePlayer target) {
        this.target = target;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {

        FootPlayer footPlayer = FootballPlugin.INSTANCE.getPlayerService().getPlayerData(target);

        ClickableItem[] seasonsItems = new ClickableItem[footPlayer.getStats().size()];

        int i = 0;
        for(int season : footPlayer.getMmr().keySet()) {
            seasonsItems[i] = ClickableItem.on(getSeasonItem(season), onClick -> {
                FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildPlayerStatsMenu(target, season), player);
            });
            i++;
        }

        SimplePagination pagination = provider.newPagination(seasonsItems, 7, new InventorySlotGroup(new InventorySlot(2, 2), new InventorySlot(2, 8)), new CubicIterator());

        if(pagination.getPageNumber() > 0)
            provider.setItem(5, 6, ClickableItem.on(createNextArrowItem(), onClick -> {
                pagination.nextPage();
                pagination.render();
            }));

        pagination.onUpdate(simplePagination -> {

            if(simplePagination.getPageNumber() <= 0) {
                provider.setItem(5, 4, ClickableItem.empty(new ItemStack(Material.AIR)));
            }
            else {
                provider.setItem(5, 4, ClickableItem.on(createPreviousArrowItem(), onClick -> {
                    pagination.previousPage();
                    pagination.render();
                }));
            }

            if(simplePagination.getPageNumber() >= simplePagination.getNumberOfPages()) {
                provider.setItem(5, 6, ClickableItem.empty(new ItemStack(Material.AIR)));
            }
            else {
                provider.setItem(5, 6, ClickableItem.on(createNextArrowItem(), onClick -> {
                    pagination.nextPage();
                    pagination.render();
                }));
            }

        });

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getSeasonItem(int season) {
        return new ItemBuilder(Material.PAPER)
                .setName("§9Season " + season)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to select this season!")
                .toItemStack();
    }

    private ItemStack createPreviousArrowItem() {
        return new ItemBuilder(CustomHeads.fromBase64(CustomListFiller.BLACK_BACKWARD_ARROW_TEXTURE))
                .setName("§bPrevious page")
                .toItemStack();
    }

    private ItemStack createNextArrowItem() {
        return new ItemBuilder(CustomHeads.fromBase64(CustomListFiller.BLACK_FORWARD_ARROW_TEXTURE))
                .setName("§bNext page")
                .toItemStack();
    }

}
