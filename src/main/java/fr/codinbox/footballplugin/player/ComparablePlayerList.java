package fr.codinbox.footballplugin.player;

import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

public class ComparablePlayerList extends ArrayList<ComparablePlayer> {

    public ComparablePlayerList() {
    }

    public int getPosition(OfflinePlayer player) {
        for(int i = 0; i < size(); i++) {
            if(player.getUniqueId().equals(get(i).getPlayer().getOfflinePlayer().getUniqueId()))
                return size() - i;
        }
        return size();
    }

}
