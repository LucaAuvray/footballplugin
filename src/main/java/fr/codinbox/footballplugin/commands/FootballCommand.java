package fr.codinbox.footballplugin.commands;

import com.google.common.collect.Lists;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.game.Arena;
import fr.codinbox.footballplugin.game.CustomArena;
import fr.codinbox.footballplugin.game.GameState;
import fr.codinbox.footballplugin.inventory.ListedInventories;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.map.MinecraftWorld;
import fr.codinbox.footballplugin.service.MinecraftWorldService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FootballCommand implements CommandExecutor {

    private final FootballPlugin plugin;
    private final LanguageManager languageManager;
    private final MinecraftWorldService worldService;

    public FootballCommand(FootballPlugin plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.worldService = plugin.getWorldService();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            // If no args open play menu
            if(args.length == 0) {
                onCommand(player);
                return true;
            }

            final String initialArgument = args[0].toLowerCase(Locale.ROOT);

            switch (initialArgument) {
                case "help":
                    sendHelp(player);
                    break;
                case "team":
                    onTeam(player);
                    break;
                case "start":
                    onStart(player);
                    break;
                case "stop":
                    onStop(player);
                    break;
                case "leave":
                    onLeave(player);
                    break;
                case "stats":
                    onStats(player, args);
                    break;
                case "spectate":
                    onSpectate(player, args);
                    break;
                case "admin":
                    onAdmin(player, args);
                    break;
            }
        }

        return true;
    }

    private void onCommand(Player player) {
        plugin.getInventoryService().openInventory(ListedInventories.buildFootballMenu(), player);
    }

    private void onStats(Player player, String[] args) {
        if(args.length < 2) {
            sendNEA(player);
            return;
        }

        String targetName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

        if(targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        FootballPlugin.INSTANCE.getInventoryService().openInventory(ListedInventories.buildPlayerStatsMenu(targetPlayer, FootConfig.CURRENT_SEASON), player);
    }

    private void sendHelp(Player player) {
        Arena playerArena = plugin.getArenaManager().getCurrentArena(player);
        boolean isInArena = playerArena != null;
        boolean isInCustomArena = playerArena instanceof CustomArena;

        player.sendMessage("§aFootballLeague §8- §9Help menu");
        player.sendMessage("§b/fb §8- §7Open the in-game football menu");
        player.sendMessage("§b/fb help §8- §7Display this help paragraph");
        if(isInCustomArena) {
            boolean isOwner = playerArena.getOwner().equals(player.getUniqueId());
            player.sendMessage("§b/fb team §8- §7Choose a team in a custom arena");
            if(isOwner) {
                player.sendMessage("§b/fb start §8- §7Start the match");
                player.sendMessage("§b/fb stop §8- §7Stop the match");
            }
        }
        player.sendMessage("§b/fb leave §8- §7Leave your current match");
        player.sendMessage("§b/fb stats <player> §8- §7Show player statistics");
        player.sendMessage("§b/fb spectate <player> §8- §7Spectate a player game");
        if(player.hasPermission("football.admin")) {
            player.sendMessage("§b/fb admin §8 - §7Display the admin help paragraph");
        }
    }

    private void onTeam(Player player) {
        Arena playerArena = plugin.getArenaManager().getCurrentArena(player);

        boolean isInCustomArena = playerArena instanceof CustomArena;

        if(playerArena == null || !isInCustomArena) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NOT_IN_ARENA));
            return;
        }

        plugin.getInventoryService().openInventory(ListedInventories.buildCustomTeamMenu(player, (CustomArena) playerArena), player);
    }

    private void onStart(Player player) {
        Arena playerArena = plugin.getArenaManager().getCurrentArena(player);

        boolean isInCustomArena = playerArena instanceof CustomArena;

        if(playerArena == null || !isInCustomArena) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NOT_IN_ARENA));
            return;
        }

        if(!playerArena.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.CUSTOM_NOT_OWNER));
            return;
        }

        if(GameState.WAITING.equals(playerArena.getCurrentState()))
            playerArena.setState(GameState.STARTING);
        else
            player.sendMessage(new LanguageManager.Phrase(LanguageKey.GAME_ALREADY_STARTED_MESSAGE).toString());
    }

    private void onStop(Player player) {
        Arena playerArena = plugin.getArenaManager().getCurrentArena(player);

        boolean isInCustomArena = playerArena instanceof CustomArena;

        if(playerArena == null || !isInCustomArena) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NOT_IN_ARENA));
            return;
        }

        if(!playerArena.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.CUSTOM_NOT_OWNER));
            return;
        }

        playerArena.setState(GameState.ENDING);
        playerArena.leavePlayer(player);
    }

    private void onLeave(Player player) {
        Arena arena = plugin.getArenaManager().getCurrentArena(player);
        if(arena != null)
            arena.leavePlayer(player);
        else
            player.sendMessage(languageManager.getPhrase(LanguageKey.NOT_IN_ARENA));
    }

    private void onSpectate(Player player, String[] args) {
        if(args.length < 2) {
            sendNEA(player);
            return;
        }

        if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null) {
            player.sendMessage(new LanguageManager.Phrase(LanguageKey.ALREADY_IN_GAME_MESSAGE).toString());
            return;
        }

        String targetName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if(targetPlayer == null) {
            player.sendMessage("§cPlayer not found!");
            return;
        }

        if(targetPlayer == player) {
            player.sendMessage("You can't spectate yourself O_O");
            return;
        }

        Arena targetPlayerArena = plugin.getArenaManager().getCurrentArena(targetPlayer);
        if(targetPlayerArena == null) {
            player.sendMessage("§cPlayer isn't in an arena!");
            return;
        }

        if(targetPlayerArena instanceof CustomArena) {
            player.sendMessage(new LanguageManager.Phrase(LanguageKey.CANT_SPECTATE_CUSTOM_GAME).toString());
            return;
        }

        if(targetPlayerArena.getSpectators().contains(targetPlayer)) {
            player.sendMessage("§cCan't spectate a spectator!");
            return;
        }

        targetPlayerArena.joinPlayer(player, true);
    }

    private void onAdmin(Player player, String[] args) {
        if(!player.hasPermission("football.admin")) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NO_PERMISSION));
            return;
        }

        if(args.length <= 1) {
            sendAdminHelp(player);
            return;
        }

        final String secondArgument = args[1].toLowerCase(Locale.ROOT);

        switch (secondArgument) {
            case "map":
                onMap(player, args);
                break;
            case "ball":
                onBall(player);
                break;
            case "save":
                onSave(player);
                break;
        }
    }

    private void sendAdminHelp(Player player) {
        if(!player.hasPermission("football.admin")) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NO_PERMISSION));
            return;
        }

        player.sendMessage("§aFootballLeague §8- §9Admin help menu");
        player.sendMessage("§b/fb admin map §8- §7Display the maps help paragraph");
        player.sendMessage("§b/fb admin ball §8- §7Drop a ball at your current position");
        player.sendMessage("§b/fb admin save §8- §7Save every files");
    }

    private void onSave(Player player) {
        try {
            plugin.getWorldService().saveMaps();
            player.sendMessage("§aFiles saved!");
        }
        catch (Exception exception) {
            exception.printStackTrace();
            player.sendMessage("§cUnable to save data files. Data loss imminent!");
        }
    }

    private void onMap(Player player, String[] args) {
        if(args.length <= 2) {
            sendMapHelp(player);
            return;
        }

        final String thirdArgument = args[2].toLowerCase(Locale.ROOT);

        switch (thirdArgument) {
            case "list":
                sendMapList(player);
                break;
            case "create":
                createMap(player, args);
                break;
            case "edit":
                editMap(player, args);
                break;
            case "delete":
                deleteMap(player, args);
                break;
            case "exit":
                exitMap(player);
                break;
            case "menu":
                openMapMenu(player);
                break;
        }
    }

    private void sendMapHelp(Player player) {
        if(!player.hasPermission("football.admin")) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NO_PERMISSION));
            return;
        }

        player.sendMessage("§aFootballLeague §8- §9Map help menu");
        player.sendMessage("§b/fb admin map list §8- §7List all available maps");
        player.sendMessage("§b/fb admin map create <name> §8- §7Create a map and edit it");
        player.sendMessage("§b/fb admin map edit <name> §8- §7Edit a map");
        player.sendMessage("§b/fb admin map delete <name> §8- §7Delete a map");
        player.sendMessage("§b/fb admin map exit §8- §7Exit a map (and save it)");
        player.sendMessage("§b/fb admin map menu §8- §7Open the map menu");
    }

    private void sendMapList(Player player) {
        if(!player.hasPermission("football.admin")) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NO_PERMISSION));
            return;
        }

        ArrayList<FootMap> maps = Lists.newArrayList(worldService.getMaps());
        StringBuilder builder = new StringBuilder();

        player.sendMessage("§7Available maps (" + maps.size() + "):");
        if(maps.size() > 0) {
            builder.append(maps.get(0).getName());
            for(FootMap map : maps.subList(1, maps.size()))
                builder.append(", " + map.getName());
        }
        else
            builder.append("No map found.");
        player.sendMessage("§b" + builder.toString());
    }

    private void createMap(Player player, String[] args) {
        if(args.length <= 3) {
            sendNEA(player);
            return;
        }

        final String mapName = args[3];

        boolean mapExist = worldService.getMapByName(mapName) != null;
        if(mapExist) {
            player.sendMessage(
                    new LanguageManager.Phrase(languageManager, LanguageKey.MAP_NAME_ALREADY_EXIST)
                    .replaceVar("name", mapName)
                    .toString()
            );
            return;
        }

        FootMap map = worldService.createMap(mapName);
        player.sendMessage(
                new LanguageManager.Phrase(languageManager, LanguageKey.MAP_LOADING)
                        .replaceVar("name", mapName)
                        .toString()
        );

        MinecraftWorld minecraftWorld = worldService.loadWorld(map, true);

        player.teleport(minecraftWorld.getSpawnLocation());

        player.sendMessage(languageManager.getPhrase(LanguageKey.MAP_TELEPORTED));
    }

    private void editMap(Player player, String[] args) {
        if(args.length <= 3) {
            sendNEA(player);
            return;
        }

        final String mapName = args[3];

        FootMap map = worldService.getMapByName(mapName);
        if(map == null) {
            player.sendMessage(
                    new LanguageManager.Phrase(languageManager, LanguageKey.MAP_NAME_NOT_EXIST)
                            .replaceVar("name", mapName)
                            .toString()
            );
            return;
        }

        player.sendMessage(
                new LanguageManager.Phrase(languageManager, LanguageKey.MAP_LOADING)
                        .replaceVar("name", mapName)
                        .toString()
        );

        MinecraftWorld minecraftWorld = worldService.loadWorld(map, true);

        player.teleport(minecraftWorld.getSpawnLocation());

        player.sendMessage(languageManager.getPhrase(LanguageKey.MAP_TELEPORTED));
    }

    private void deleteMap(Player player, String[] args) {
        if(args.length <= 3) {
            sendNEA(player);
            return;
        }

        final String mapName = args[3];

        FootMap map = worldService.getMapByName(mapName);
        if(map == null) {
            player.sendMessage(
                    new LanguageManager.Phrase(languageManager, LanguageKey.MAP_NAME_NOT_EXIST)
                            .replaceVar("name", mapName)
                            .toString()
            );
            return;
        }

        MinecraftWorld minecraftWorld = plugin.getWorldService().getEditingWorld(map.getMode());
        if(minecraftWorld != null)
            minecraftWorld.destroy(true);

        plugin.getWorldService().deleteMap(map);

        player.sendMessage(new LanguageManager.Phrase(LanguageKey.MAP_DELETED)
            .replaceVar("name", mapName)
            .toString());
    }

    private void exitMap(Player player) {
        MinecraftWorld minecraftWorld = worldService.getWorldByBukkitWorld(player.getWorld());

        if(minecraftWorld == null) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NO_EDITING_MAP));
            return;
        }

        player.sendMessage(
                new LanguageManager.Phrase(languageManager, LanguageKey.MAP_UNLOADING)
                        .replaceVar("name", minecraftWorld.getName())
                        .toString()
        );

        worldService.unloadWorld(minecraftWorld);
    }

    private void openMapMenu(Player player) {
        MinecraftWorld minecraftWorld = worldService.getWorldByBukkitWorld(player.getWorld());

        if(minecraftWorld == null) {
            player.sendMessage(languageManager.getPhrase(LanguageKey.NO_EDITING_MAP));
            return;
        }

        plugin.getInventoryService().openInventory(ListedInventories.buildWorldEditorMenu(minecraftWorld.getParentMap()), player);
    }

    private void onBall(Player player) {
        this.plugin.getBallManager().spawnBall(player.getLocation());
        player.sendMessage(languageManager.getPhrase(LanguageKey.POOF));
    }

    private void sendUnavailable(Player player) {
        player.sendMessage(languageManager.getPhrase(LanguageKey.COMMAND_UNAVAILABLE));
    }

    private void sendNEA(Player player) {
        player.sendMessage(languageManager.getPhrase(LanguageKey.COMMAND_NOT_ENOUGH_ARGUMENTS));
    }

}
