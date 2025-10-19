package fr.codinbox.footballplugin.inventory;

public interface PagerIterator {

    void setSlotGroup(InventorySlotGroup group);

    void reset();

    InventorySlot getNext(int i);

}
