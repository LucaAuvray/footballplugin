package fr.codinbox.footballplugin.player.action;

import org.bukkit.entity.Player;

public class SendMessageAction implements ExecutableAction {

    private final String message;

    public SendMessageAction(String message) {
        this.message = message;
    }

    @Override
    public void execute(Player player) {
        player.sendMessage(message);
    }

}
