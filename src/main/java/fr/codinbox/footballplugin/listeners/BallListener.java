package fr.codinbox.footballplugin.listeners;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.Ball;
import fr.codinbox.footballplugin.game.BallManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class BallListener implements Listener {

    private FootballPlugin plugin;
    private BallManager ballManager;

    public BallListener(FootballPlugin plugin) {
        this.plugin = plugin;
        this.ballManager =  plugin.getBallManager();
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if(ballManager.isBall(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBallPicked(PlayerPickupItemEvent event) {
        if(ballManager.isBall(event.getItem()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBallKicked(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        for(Ball ball : ballManager.getNearbyBalls(player.getLocation()))
            ball.kicked(player);
    }

}
