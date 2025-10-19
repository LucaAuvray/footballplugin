package fr.codinbox.footballplugin.utils;

import fr.codinbox.footballplugin.FootballPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.function.Consumer;

public class ChatUtils {

    private final static int CENTER_PX = 154;

    private static HashMap<Player, Consumer<AsyncPlayerChatEvent>> chatConsumer;

    static {
        chatConsumer = new HashMap<>();
    }

    public static void sendCenteredMessage(Player player, String message){
        if(message == null || message.equals("")) player.sendMessage("");
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb.toString() + message);
    }

    public static void doOnNextMessage(Player player, Consumer<AsyncPlayerChatEvent> consumer) {
        chatConsumer.put(player, consumer);
    }

    public static void messageReceived(Player player, AsyncPlayerChatEvent event) {
        if(chatConsumer.containsKey(player)) {
            final Consumer<AsyncPlayerChatEvent> consumer = chatConsumer.get(player);
            chatConsumer.remove(player);
            Bukkit.getScheduler().runTask(FootballPlugin.INSTANCE, () -> {
                consumer.accept(event);
            });
            event.setCancelled(true);
        }
    }

}
