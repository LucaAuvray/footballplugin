package fr.codinbox.footballplugin.player.action;

import org.bukkit.entity.Player;


public class SetLevelAction implements ExecutableAction {

    private int level;

    public SetLevelAction(int level) {
        this.level = level;
    }

    @Override
    public void execute(Player player) {
        player.setLevel(level);
    }

}
