package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.inventory.ListedInventories;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.service.InventoryService;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import fr.codinbox.footballplugin.utils.SerializablePosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class WorldEditorFiller implements IFiller {

    private final InventoryService inventoryService;
    private final FootMap map;

    public WorldEditorFiller(FootMap map) {
        this.inventoryService = FootballPlugin.INSTANCE.getInventoryService();
        this.map = map;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {
        ItemStack modeItem = getModeItem(map.getMode());
        ItemStack blueTeamItem = getTeamSpawnItem(FootTeamQualifier.BLUE);
        ItemStack redTeamItem = getTeamSpawnItem(FootTeamQualifier.RED);
        ItemStack centerItem = getSpawnItem("Center", map.getCenterLocation(player.getWorld()));

        provider.setItem(0, ClickableItem.on(modeItem, onClick -> {
            int currentModeId = FootMode.getItemPosition(map.getMode());
            int nextModeId = (FootMode.values().length - 1) > currentModeId ? currentModeId + 1 : 0;
            map.setMode(FootMode.values()[nextModeId]);

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);

            provider.setItem(onClick.getSlot(), ClickableItem.previous(getModeItem(map.getMode())));
        }));

        provider.setItem(1, ClickableItem.on(blueTeamItem, onClick ->
                inventoryService.openInventory
                        (ListedInventories.buildSpawnEditorMenu(map, FootTeamQualifier.BLUE), player)));

        provider.setItem(2, ClickableItem.on(redTeamItem, onClick ->
                inventoryService.openInventory
                        (ListedInventories.buildSpawnEditorMenu(map, FootTeamQualifier.RED), player)));

        provider.setItem(3, ClickableItem.on(centerItem, onClick -> {
            ClickType clickType = onClick.getClick();
            Location centerLocation = map.getCenterLocation(player.getWorld());
            switch (clickType) {
                case LEFT:
                    if(centerLocation != null)
                        player.teleport(centerLocation);
                    break;

                case RIGHT:
                    map.setCenterLocation(new SerializablePosition(player.getLocation()));
                    player.closeInventory();
                    player.sendMessage(new LanguageManager.Phrase(LanguageKey.SPAWNPOINT_SET_NO_INFOS).toString());
                    break;
            }
        }));
    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getModeItem(FootMode mode) {
        return new ItemBuilder(Material.ANVIL)
                .setName("§b" + mode.getModeName() + " " + (mode.getSubModeName() != null ? mode.getSubModeName() : ""))
                .addLoreLine("§r")
                .addLoreLine("§7Mmr activated: §9" + mode.hasMmrActivated())
                .addLoreLine("§7Spawns/Team: §9" + (mode.getNumberOfSpawns() == -1 ? "∞" : mode.getNumberOfSpawns()))
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to switch mode")
                .addLoreLine("§cWARNING: Clicking this button resets every team spawns!")
                .toItemStack();
    }

    private ItemStack getTeamSpawnItem(FootTeamQualifier team) {
        int numberOfSpawns = map.getMode().getNumberOfSpawns();
        return new ItemBuilder(team.getItem())
                .setName("§bEdit " + team.getLanguageName() + " team spawn(s)")
                .addLoreLine("§e" + map.getTeamSpawns().get(team).size() + "/" + (numberOfSpawns > 0 ? numberOfSpawns : "∞") + " spawns defined")
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to edit spawns")
                .toItemStack();
    }

    private ItemStack getSpawnItem(String label, Location location) {
        return new ItemBuilder(Material.BEACON)
                .setName("§b" + label + " spawn")
                .addLoreLine("§r")
                .addLoreLine("§7Coordinates:")
                .addLoreLine("§7X: " + (location != null ? String.valueOf(location.getX()) : "§c#.#####"))
                .addLoreLine("§7Y: " + (location != null ? String.valueOf(location.getY()) : "§c#.#####"))
                .addLoreLine("§7Z: " + (location != null ? String.valueOf(location.getZ()) : "§c#.#####"))
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to teleport")
                .addLoreLine("§eRight-click to set")
                .toItemStack();
    }

}
