package fr.codinbox.footballplugin.player;

import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.mode.FootMode;

public class ComparablePlayer implements Comparable<ComparablePlayer> {

    protected final FootPlayer player;
    protected final FootMode mode;

    public ComparablePlayer(FootPlayer player, FootMode mode) {
        this.player = player;
        this.mode = mode;
    }

    public FootPlayer getPlayer() {
        return player;
    }

    public FootMode getMode() {
        return mode;
    }

    @Override
    public int compareTo(ComparablePlayer otherPlayer) {
        double playerMmr = player.getMmr().get(FootConfig.CURRENT_SEASON).get(mode);
        double otherPlayerMmr = otherPlayer.player.getMmr().get(FootConfig.CURRENT_SEASON).get(mode);
        return Double.compare(playerMmr, otherPlayerMmr);
    }

}
