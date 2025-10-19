package fr.codinbox.footballplugin.listeners;

import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.game.Arena;
import fr.codinbox.footballplugin.inventory.ListedInventories;
import fr.codinbox.footballplugin.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private FootballPlugin plugin;

    public PlayerListener(FootballPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(plugin.getArenaManager().isInGame(player))
                event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(plugin.getArenaManager().isInGame(player))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(plugin.getFrozePlayers().containsKey(player)) {
            player.teleport(plugin.getFrozePlayers().get(player).toLocation());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if(plugin.getArenaManager().isInGame(player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(plugin.getArenaManager().isInGame(player))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if(plugin.getArenaManager().isInGame(player))
            event.setRespawnLocation(plugin.getArenaManager().getCurrentArena(player).teleportPlayerToSpawn(player));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(plugin.getPlayerService().getPlayerData(player) == null)
            player.kickPlayer("§cUnable to load your foot datas (Please contact the server admins)");
        Arena arena = plugin.getArenaManager().getCurrentArena(player);
        if(arena != null) {
            arena.joinPlayer(player, false);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerService().markAsSaving(player);
        Arena arena = plugin.getArenaManager().getCurrentArena(player);
        if(arena != null) {
            arena.leavePlayer(player);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ChatUtils.messageReceived(player, event);
    }

}
