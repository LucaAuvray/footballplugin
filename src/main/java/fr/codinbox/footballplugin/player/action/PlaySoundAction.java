package fr.codinbox.footballplugin.player.action;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PlaySoundAction implements ExecutableAction {

    private final Location location;
    private final Sound sound;
    private final float volume;
    private final float pitch;

    public PlaySoundAction(Location location, Sound sound, float volume, float pitch) {
        this.location = location;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void execute(Player player) {
        if(this.location != null)
            player.playSound(location, sound, volume, pitch);
        else
            player.playSound(player.getLocation(), sound, volume, pitch);
    }

}
