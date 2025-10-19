package fr.codinbox.footballplugin.player.action;

import fr.codinbox.footballplugin.game.Arena;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class PlayerExecutor {

    private Arena arena;

    public PlayerExecutor(Arena arena) {
        this.arena = arena;
    }

    public void broadcastMessage(String message) {
        multipleAction().sendMessage(message);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        multipleAction().playSound(sound, volume, pitch);
    }

    public void setLevel(int level) {
        multipleAction().setLevel(level);
    }

    public PlayerUnit on(Player... players) {
        return new PlayerUnit(Arrays.asList(players));
    }

    public PlayerUnit multipleAction() {
        return new PlayerUnit(arena.getAllPlayers());
    }

    public PlayerUnit multipleIngameAction() {
        return new PlayerUnit(arena.getIngamePlayers());
    }

    public PlayerUnit multipleSpectateAction() {
        return new PlayerUnit(arena.getSpectators());
    }

    public static class PlayerUnit {

        private final List<OfflinePlayer> players;
        private final ArrayList<ActionExecutor> actions;

        public PlayerUnit(List<OfflinePlayer> players) {
            this.players = players;
            this.actions = new ArrayList<>();
        }

        public PlayerUnit addExecutor(Consumer<ActionExecutor> executor) {
            ActionExecutor exec = new ActionExecutor();
            executor.accept(exec);
            if(!exec.getActions().isEmpty())
                this.actions.add(exec);
            return this;
        }

        public void sendMessage(String message) {
            players.forEach(p -> {
                if(p.isOnline())
                    p.getPlayer().sendMessage(message);
            });
        }

        public void playSound(Location location, Sound sound, float volume, float pitch) {
            players.forEach(p -> {
                if(p.isOnline())
                    p.getPlayer().playSound(location, sound, volume, pitch);
            });
        }

        public void playSound(Sound sound, float volume, float pitch) {
            players.forEach(p -> {
                if(p.isOnline())
                    p.getPlayer().playSound(p.getPlayer().getLocation(), sound, volume, pitch);
            });
        }

        public void setLevel(int level) {
            players.forEach(p -> {
                if(p.isOnline())
                    p.getPlayer().setLevel(level);
            });
        }

        public void executeActions() {
            ArrayList<ExecutableAction> actions  = new ArrayList<>();
            this.actions.forEach(actionExecutor -> actions.addAll(actionExecutor.getActions()));
            actions.forEach(action -> { players.forEach(player -> {
                if(player.isOnline())
                    action.execute(player.getPlayer());
            }); });
        }

    }

}
