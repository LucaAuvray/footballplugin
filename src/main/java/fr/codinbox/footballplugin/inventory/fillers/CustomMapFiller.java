package fr.codinbox.footballplugin.inventory.fillers;

import com.google.common.collect.Lists;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.CustomGameCriterias;
import fr.codinbox.footballplugin.inventory.*;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.CustomHeads;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class CustomMapFiller implements IFiller {

    private static final String BLACK_FORWARD_ARROW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDllY2NjNWMxYzc5YWE3ODI2YTE1YTdmNWYxMmZiNDAzMjgxNTdjNTI0MjE2NGJhMmFlZjQ3ZTVkZTlhNWNmYyJ9fX0=";
    private static final String BLACK_BACKWARD_ARROW_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY0Zjc3OWE4ZTNmZmEyMzExNDNmYTY5Yjk2YjE0ZWUzNWMxNmQ2NjllMTljNzVmZDFhN2RhNGJmMzA2YyJ9fX0=";

    private CustomGameCriterias criterias;

    public CustomMapFiller(CustomGameCriterias criterias) {
        this.criterias = criterias;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {

        ArrayList<FootMap> maps = Lists.newArrayList(FootballPlugin.INSTANCE.getWorldService().getMaps(FootMode.CUSTOM));
        ClickableItem[] items = new ClickableItem[maps.size()];
        for(int i = 0; i < items.length; i++) {
            FootMap map = maps.get(i);
            items[i] = ClickableItem.on(createMapItem(map), onClick -> {
                criterias.setMap(map);
                FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildCustomCreateMenu(player), player);
            });
        }

        Pagination pagination = provider.newPagination(items, 45,
                new InventorySlotGroup(new InventorySlot(1, 1), new InventorySlot(5, 9)),
                new CubicIterator());

        provider.setItem(6, 4, ClickableItem.on(createPreviousArrowItem(), onClick -> {
            pagination.previousPage();
            pagination.render();
        }));

        provider.setItem(6, 6, ClickableItem.on(createNextArrowItem(), onClick -> {
            pagination.nextPage();
            pagination.render();
        }));

    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack createPreviousArrowItem() {
        return new ItemBuilder(CustomHeads.fromBase64(BLACK_BACKWARD_ARROW_TEXTURE))
                .setName("§bPrevious page")
                .toItemStack();
    }

    private ItemStack createNextArrowItem() {
        return new ItemBuilder(CustomHeads.fromBase64(BLACK_FORWARD_ARROW_TEXTURE))
                .setName("§bNext page")
                .toItemStack();
    }

    private ItemStack createMapItem(FootMap map) {
        int blueTeamSpawns = map.getTeamSpawns().get(FootTeamQualifier.BLUE).size();
        int redTeamSpawns = map.getTeamSpawns().get(FootTeamQualifier.RED).size();
        return new ItemBuilder(Material.GRASS)
                .setName("§b" + map.getName())
                .addLoreLine("§r")
                .addLoreLine("§7Number of spawns: §b" + blueTeamSpawns + "§8+§c" + redTeamSpawns)
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to select this map")
                .toItemStack();
    }

}
