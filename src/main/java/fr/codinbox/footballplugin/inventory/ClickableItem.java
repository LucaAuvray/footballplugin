package fr.codinbox.footballplugin.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class ClickableItem {

    private ItemStack item;
    private Consumer<InventoryClickEvent> consumer;
    private boolean previous;

    public ClickableItem(ItemStack item, Consumer<InventoryClickEvent> consumer, boolean previous) {
        this.item = item;
        this.consumer = consumer;
        this.previous = previous;
    }

    public static ClickableItem on(ItemStack itemStack, Consumer<InventoryClickEvent> consumer) {
        return new ClickableItem(itemStack, consumer, false);
    }

    public static ClickableItem empty(ItemStack itemStack) {
        return new ClickableItem(itemStack, event -> {}, false);
    }

    public static ClickableItem previous(ItemStack itemStack) {
        return new ClickableItem(itemStack, null, true);
    }

    public void onClick(InventoryClickEvent event) {
        this.consumer.accept(event);
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isPrevious() {
        return previous;
    }

}
