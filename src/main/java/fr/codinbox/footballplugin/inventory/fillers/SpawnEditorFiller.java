package fr.codinbox.footballplugin.inventory.fillers;

import fr.codinbox.footballplugin.inventory.ClickableItem;
import fr.codinbox.footballplugin.inventory.IFiller;
import fr.codinbox.footballplugin.inventory.InventoryProvider;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.ItemBuilder;
import fr.codinbox.footballplugin.utils.SerializablePosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class SpawnEditorFiller implements IFiller {

    private final FootMap map;
    private final FootTeamQualifier qualifier;

    public SpawnEditorFiller(FootMap map, FootTeamQualifier qualifier) {
        this.map = map;
        this.qualifier = qualifier;
    }

    @Override
    public void onOpen(InventoryProvider provider, Player player) {
        ArrayList<SerializablePosition> spawnLocations = map.getTeamSpawns().get(this.qualifier);
        for (int i = 0; i < spawnLocations.size(); i++) {
            SerializablePosition location = spawnLocations.get(i);
            provider.setItem(i, ClickableItem.on(getSpawnItem(i + 1, location.toLocation(player.getWorld())), onClick -> {
                ClickType clickType = onClick.getClick();

                switch (clickType) {
                    case LEFT:
                        Location sLocation = location.toLocation(player.getWorld());
                        player.teleport(sLocation);
                        player.closeInventory();
                        break;

                    case RIGHT:
                        map.deleteTeamSpawn(qualifier, location);
                        onOpen(provider, player);
                        break;
                }
            }));
        }

        provider.setItem(spawnLocations.size(), ClickableItem.on(getCreateSpawnItem(), onClick -> {
            player.closeInventory();

            Location playerLocation = player.getLocation();
            SerializablePosition serializablePosition = new SerializablePosition(playerLocation);

            map.addTeamSpawn(qualifier, serializablePosition);

            player.sendMessage(new LanguageManager.Phrase(LanguageKey.SPAWNPOINT_SET)
                    .replaceVar("current", String.valueOf(map.getTeamSpawns().get(this.qualifier).size()))
                    .replaceVar("max", String.valueOf(map.getMode().getNumberOfSpawns() > 0 ? map.getMode().getNumberOfSpawns() : "∞"))
                    .toString());
        }));
    }

    @Override
    public void onTick(InventoryProvider provider, Player player) {

    }

    private ItemStack getSpawnItem(int number, Location location) {
        return new ItemBuilder(Material.BEACON, number)
                .setName("§bSpawn #" + number)
                .addLoreLine("§r")
                .addLoreLine("§7Coordinates:")
                .addLoreLine("§7X: " + location.getX())
                .addLoreLine("§7Y: " + location.getY())
                .addLoreLine("§7Z: " + location.getZ())
                .addLoreLine("§r")
                .addLoreLine("§eLeft-click to teleport")
                .addLoreLine("§eRight-click to delete")
                .toItemStack();
    }

    private ItemStack getCreateSpawnItem() {
        int numberOfSpawns = this.map.getTeamSpawns().get(this.qualifier).size();
        int maxSpawns = this.map.getMode().getNumberOfSpawns();
        int numberOfSpawnsLeft = maxSpawns - numberOfSpawns;

        if(numberOfSpawnsLeft > 0 || maxSpawns == -1)
            return new ItemBuilder(Material.OAK_BUTTON)
                    .setName("§aCreate a new spawn")
                    .addLoreLine("§e" + (maxSpawns > -1 ? numberOfSpawnsLeft : "∞") + " remaining")
                    .toItemStack();
        else
            return new ItemStack(Material.AIR);
    }

}
