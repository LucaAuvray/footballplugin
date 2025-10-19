package fr.codinbox.footballplugin.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class InventoryProvider {

    private final Inventory inventory;
    private final ClickableItem[] previousItems;
    private final ClickableItem[] items;
    private final ArrayList<Pagination> paginations;

    protected InventoryProvider(Inventory inventory) {
        this.inventory = inventory;
        this.previousItems = new ClickableItem[inventory.getSize()];
        this.items = new ClickableItem[inventory.getSize()];
        this.paginations = new ArrayList<>();
    }

    protected int rowColToSlot(int row, int column) {
        return 9*(row-1)+(column-1);
    }

    public void setItem(int slot, ClickableItem item) {
        if(items[slot] != null)
            if(previousItems[slot] == null || !items[slot].isPrevious())
                previousItems[slot] = items[slot];
        items[slot] = item;
        inventory.setItem(slot, item.getItem());
    }

    public void setItem(int row, int column, ClickableItem item) {
        setItem(9*(row-1)+(column-1), item);
    }

    public void fill(ClickableItem item) {
        for(int i = 0; i < this.inventory.getSize(); i++)
            setItem(i, item);
    }

    public SimplePagination newPagination(ClickableItem[] items, int itemsPerPage, InventorySlotGroup fillableArea, PagerIterator iterator) {
        iterator.setSlotGroup(fillableArea);

        SimplePagination simplePagination = new SimplePagination(this, items, itemsPerPage, 0, iterator);

        this.paginations.add(simplePagination);

        simplePagination.render();

        return simplePagination;
    }

    protected void interact(int slot, InventoryClickEvent event) {
        if(items.length >= slot + 1) {
            ClickableItem clickableItem = items[slot];
            if(clickableItem.isPrevious()) {
                if (previousItems[slot] != null)
                    previousItems[slot].onClick(event);
            }
            else {
                if (items[slot] != null)
                    items[slot].onClick(event);
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

}
