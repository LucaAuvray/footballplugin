package fr.codinbox.footballplugin.inventory;

public class CubicIterator implements PagerIterator {

    private int slotRow;
    private int slotCol;

    private InventorySlotGroup slotGroup;

    private InventorySlot getFirstSlot() {
        return slotGroup.getSlots().get(0);
    }

    private InventorySlot getLastSlot() {
        return slotGroup.getSlots().get(slotGroup.getSlots().size() - 1);
    }

    @Override
    public void setSlotGroup(InventorySlotGroup group) {
        this.slotGroup = group;
        this.slotRow = slotGroup.getSlots().get(0).getRow();
        this.slotCol = slotGroup.getSlots().get(0).getCol();
    }

    @Override
    public void reset() {
        setSlotGroup(slotGroup);
    }

    @Override
    public InventorySlot getNext(int i) {
        int row = slotRow, col = slotCol;

        if(slotCol < getLastSlot().getCol()+1)
            slotCol++;
        else {
            slotCol = getFirstSlot().getCol();
            if(slotRow < getLastSlot().getRow()+1)
                slotRow++;
        }

        return new InventorySlot(row, col);
    }

}
