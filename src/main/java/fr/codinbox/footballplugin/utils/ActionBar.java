package fr.codinbox.footballplugin.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ActionBar {

    private final ArrayList<Player> players;
    private final String message;

    public ActionBar(ArrayList<OfflinePlayer> players, String message) {
        this.players = new ArrayList<>();
        players.forEach(op -> this.players.add(op.getPlayer()));
        this.message = message;
    }

    public ActionBar(Player player, String message) {
        this.players = new ArrayList<>();
        this.players.add(player);
        this.message = message;

    }

    @SuppressWarnings("deprecation")
    public void send() {
        for(Player player : this.players) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }

}
