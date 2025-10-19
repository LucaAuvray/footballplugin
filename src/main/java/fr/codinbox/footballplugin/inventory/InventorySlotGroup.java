package fr.codinbox.footballplugin.inventory;

import java.util.ArrayList;

public class InventorySlotGroup {

    private ArrayList<InventorySlot> slots;

    public InventorySlotGroup(InventorySlot startingSlot, InventorySlot endingSlot) {
        this.slots = new ArrayList<>();
        if(endingSlot.getRow() != startingSlot.getRow())
            for(int r = startingSlot.getRow(); r < endingSlot.getRow(); r++) {
                for(int c = startingSlot.getCol(); c < endingSlot.getCol(); c++) {
                    slots.add(new InventorySlot(r, c));
                }
            }
        else
            for(int c = startingSlot.getCol(); c < endingSlot.getCol(); c++) {
                slots.add(new InventorySlot(startingSlot.getRow(), c));
            }
    }

    public ArrayList<InventorySlot> getSlots() {
        return slots;
    }

}
