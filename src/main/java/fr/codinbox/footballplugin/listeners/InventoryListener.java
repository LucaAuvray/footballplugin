package fr.codinbox.footballplugin.listeners;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.inventory.OpenableInventory;
import fr.codinbox.footballplugin.service.InventoryService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final FootballPlugin plugin;
    private final InventoryService inventoryService;

    public InventoryListener(FootballPlugin plugin, InventoryService inventoryService) {
        this.plugin = plugin;
        this.inventoryService = inventoryService;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if(player.getOpenInventory() != null)
                return;

            inventoryService.unregisterOpenedInventory(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            OpenableInventory openableInventory = plugin.getInventoryService().getOrNull(player);
            if(openableInventory != null) {
                ItemStack clickedItem = event.getCurrentItem();

                if(clickedItem == null || Material.AIR.equals(clickedItem.getType()))
                    return;

                if(!event.getClickedInventory().equals(player.getOpenInventory().getTopInventory()))
                    return;

                if(openableInventory.getPlayerInventory(player) != null && openableInventory.getPlayerInventory(player).equals(event.getClickedInventory())) {
                    openableInventory.interact(event);
                    event.setCancelled(true);
                }
            }
        }
    }

}
