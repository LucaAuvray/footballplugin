package fr.codinbox.footballplugin.map;

import java.util.ArrayList;

public class FootMapCapacitor {

    private ArrayList<FootMap> maps;

    public FootMapCapacitor(ArrayList<FootMap> maps) {
        this.maps = maps;
    }

    public FootMapCapacitor() {
    }

    public ArrayList<FootMap> getMaps() {
        return maps;
    }

}
