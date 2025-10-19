package fr.codinbox.footballplugin.inventory;

import fr.codinbox.footballplugin.service.InventoryService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class InventoryServiceImpl implements InventoryService {

    private Thread inventoryUpdateThread;
    private int inventoryUpdateTaskId;

    private HashMap<Player, OpenableInventory> openedInventories;

    @Override
    public void init(Plugin plugin) {
        this.openedInventories = new HashMap<>();
        this.inventoryUpdateThread = new Thread(() -> {
            inventoryUpdateTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                openedInventories.forEach((player, inventory) -> {
                    if(inventory != null)
                        inventory.update(player);
                });
            }, 0, 1);
        });
        inventoryUpdateThread.start();
    }

    @Override
    public void exit() {
        if(inventoryUpdateThread != null && inventoryUpdateThread.isAlive()) {
            Bukkit.getScheduler().cancelTask(inventoryUpdateTaskId);
            inventoryUpdateThread.interrupt();
        }
        this.openedInventories.clear();
    }

    @Override
    public void registerOpenedInventory(OpenableInventory inventory, Player player) {
        openedInventories.put(player, inventory);
    }

    @Override
    public void unregisterOpenedInventory(Player player) {
        openedInventories.remove(player);
    }

    @Override
    public OpenableInventory getOrNull(Player player) {
        return openedInventories.getOrDefault(player, null);
    }

    @Override
    public void openInventory(OpenableInventory inventory, Player player) {
        inventory.open(player);
    }

}
