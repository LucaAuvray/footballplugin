package fr.codinbox.footballplugin.inventory;

public class InventorySlot {

    private int row;
    private int col;

    public InventorySlot(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int toInventorySlot() {
        return 9*(row-1)+(col-1);
    }

}
