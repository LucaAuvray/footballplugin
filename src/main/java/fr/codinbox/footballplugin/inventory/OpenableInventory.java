package fr.codinbox.footballplugin.inventory;

import fr.codinbox.footballplugin.FootballPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class OpenableInventory {

    private String title;
    private int size;
    private IFiller filler;
    private InventoryProvider currentProvider;
    private HashMap<Player, Inventory> playerInventory;

    public OpenableInventory(String title, int size, IFiller filler) {
        this.title = title;
        this.size = size;
        this.filler = filler;
        this.playerInventory = new HashMap<>();
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, this.size, this.title);
        this.currentProvider = new InventoryProvider(inventory);
        filler.onOpen(currentProvider, player);
        FootballPlugin.INSTANCE.getInventoryService().registerOpenedInventory(this, player);
        playerInventory.put(player, inventory);
        player.openInventory(inventory);
    }

    public void update(Player player) {
        filler.onTick(currentProvider, player);
    }

    public void interact(InventoryClickEvent event) {
        int slot = event.getSlot();
        currentProvider.interact(slot, event);
    }

    public Inventory getPlayerInventory(Player player) {
        return playerInventory.get(player);
    }

    protected IFiller getFiller() {
        return this.filler;
    }

    public static class Builder {

        private int size;
        private String title;
        private IFiller filler;

        public Builder(int size, String title) {
            this.size = size;
            this.title = title;
        }

        public Builder setFiller(IFiller filler) {
            this.filler = filler;
            return this;
        }

        public OpenableInventory build() {
            return new OpenableInventory(this.title, this.size, this.filler);
        }

    }

}
