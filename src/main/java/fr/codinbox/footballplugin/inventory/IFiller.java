package fr.codinbox.footballplugin.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface IFiller {

    void onOpen(InventoryProvider provider, Player player);

    void onTick(InventoryProvider provider, Player player);

}
