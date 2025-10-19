package fr.codinbox.footballplugin.player.action;

import org.bukkit.Location;
import org.bukkit.Sound;

import java.util.ArrayList;

public class ActionExecutor {

    private final ArrayList<ExecutableAction> actions;

    public ActionExecutor() {
        this.actions = new ArrayList<>();
    }

    public void sendMessage(String message) {
        this.actions.add(new SendMessageAction(message));
    }

    public void playSound(Location location, Sound sound, float volume, float pitch) {
        this.actions.add(new PlaySoundAction(location, sound, volume, pitch));
    }

    public void playSound(Sound sound, float volume, float pitch) {
        this.actions.add(new PlaySoundAction(null, sound, volume, pitch));
    }

    public void setLevel(int level) {
        this.actions.add(new SetLevelAction(level));
    }
    public void addCustomAction(ExecutableAction action) {
        this.actions.add(action);
    }

    public ArrayList<ExecutableAction> getActions() {
        return actions;
    }

}
