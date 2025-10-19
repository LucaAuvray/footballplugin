package fr.codinbox.footballplugin.utils;

import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class Title {

    /**
     * Send a title to player
     * @param player Player to send the title to
     * @param text The text displayed in the title
     * @param fadeInTime The time the title takes to fade in
     * @param showTime The time the title is displayed
     * @param fadeOutTime The time the title takes to fade out
     */
    public static void sendTitle(Player player, String text, int fadeInTime, int showTime, int fadeOutTime) {
        player.sendTitle(text, "", fadeInTime, showTime, fadeOutTime);

    }

}
