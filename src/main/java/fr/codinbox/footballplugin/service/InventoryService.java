package fr.codinbox.footballplugin.service;

import fr.codinbox.footballplugin.inventory.OpenableInventory;
import org.bukkit.entity.Player;

public interface InventoryService extends PluginService {

    void registerOpenedInventory(OpenableInventory inventory, Player player);

    void unregisterOpenedInventory(Player player);

    OpenableInventory getOrNull(Player player);

    void openInventory(OpenableInventory inventory, Player player);

}
