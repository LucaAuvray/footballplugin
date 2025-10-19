package fr.codinbox.footballplugin.matchmaking;

import com.google.common.collect.Lists;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.game.Arena;
import fr.codinbox.footballplugin.game.CasualArena;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.map.MinecraftWorld;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.ComparablePlayer;
import fr.codinbox.footballplugin.player.FootPlayer;
import fr.codinbox.footballplugin.service.MatchmakingService;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class MatchmakingServiceImpl implements MatchmakingService {

    private static final Logger LOGGER = Bukkit.getLogger();

    /**
     * Plugin instance
     */
    private FootballPlugin plugin;

    /**
     * Matchmaking loop ticks
     */
    private int loopTicks;

    /**
     * Maximum number of arenas
     */
    private int maxArenas;

    /**
     * Matchmaking loop task thread
     */
    private Thread loopTaskThread;

    /**
     * Matchmaking info task thread
     */
    private Thread infoTaskThread;

    /**
     * Matchmaking loop task id
     */
    private int loopTaskId;

    /**
     * Matchmaking loop task id
     */
    private int infoTaskId;

    /**
     * Info loading chars
     */
    private static final String[] infoLoading = { "|", "/", "─", "\\\\", "|", "/", "─", "\\\\" };

    /**
     * Current info loading char id
     */
    private int infoLoadingId;

    /**
     * Matchmaking queues
     */
    private HashMap<FootMode, HashMap<Player, MatchmakingCriteria>> queues;

    @Override
    public void init(Plugin plugin) {
        this.plugin = (FootballPlugin) plugin;

        // Init config values
        this.loopTicks = FootConfig.MATCHMAKING_LOOP_SECONDS;
        this.maxArenas = FootConfig.MATCHMAKING_MAX_ARENAS;

        // Create queues
        this.queues = new HashMap<>();
        for (FootMode mode : FootMode.values())
            if(!mode.equals(FootMode.CUSTOM))
                createQueue(mode);

        // Create queue loop
        if(this.loopTaskThread != null && this.loopTaskThread.isAlive())
            return;
        this.loopTaskThread = new Thread(() -> loopTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::loopTick, 0, this.loopTicks * 20L));
        loopTaskThread.start();

        // Create info task loop
        if(this.infoTaskThread != null && this.infoTaskThread.isAlive())
            return;
        this.infoTaskThread = new Thread(() -> infoTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::loopInfo, 0, 20));
        infoTaskThread.start();


        LOGGER.fine("[Football] Matchmaking service initialized with " + loopTicks + " loop ticks");
    }

    private void loopTick() {
        queues.keySet().forEach(this::processQueue);
    }

    private void loopInfo() {
        if(infoLoadingId + 1 > infoLoading.length)
            infoLoadingId = 0;

        queues.forEach((mode, queued) -> {
            queued.forEach((player, criteria) -> {
                new ActionBar(player, "§rSearching for §b" + mode.getModeName()
                        + " " + mode.getSubModeName() + " §r" + infoLoading[infoLoadingId]).send();
            });
        });

        infoLoadingId++;
    }

    @Override
    public void exit() {
        Bukkit.getScheduler().cancelTask(loopTaskId);
        if(this.loopTaskThread != null && this.loopTaskThread.isAlive())
            loopTaskThread.interrupt();

        Bukkit.getScheduler().cancelTask(infoTaskId);
        if(this.infoTaskThread != null && this.infoTaskThread.isAlive())
            infoTaskThread.interrupt();
    }

    @Override
    public void joinMatchmaking(Player player, MatchmakingCriteria criteria) {
        if(FootballPlugin.INSTANCE.getArenaManager().getCurrentArena(player) != null)
            return;

        if(criteria.getMode() == null)
            throw new IllegalArgumentException("Criteria mode cannot be null");
        if(isInMatchmaking(player))
            leaveMatchmaking(player);
        queues.get(criteria.getMode()).put(player, criteria);
        player.sendMessage(new LanguageManager.Phrase(LanguageKey.QUEUE_JOIN)
            .replaceVar("mode", criteria.getMode().getModeName())
            .replaceVar("submode", criteria.getMode().getSubModeName())
            .toString());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    @Override
    public void leaveMatchmaking(Player player) {
        MatchmakingCriteria matchmakingCriteria = getCurrentMatchmaking(player);
        if(matchmakingCriteria == null)
            return;
        queues.get(matchmakingCriteria.getMode()).remove(player);
        player.sendMessage(new LanguageManager.Phrase(LanguageKey.QUEUE_LEAVE).toString());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    @Override
    public MatchmakingCriteria getCurrentMatchmaking(Player player) {
        for(FootMode mode : this.queues.keySet()) {
            if(this.queues.get(mode).containsKey(player)) {
                return this.queues.get(mode).get(player);
            }
        }
        return null;
    }

    @Override
    public boolean isInMatchmaking(Player player) {
        return (getCurrentMatchmaking(player) != null);
    }

    @Override
    public HashMap<Player, MatchmakingCriteria> getQueuedPlayers(FootMode mode) {
        return this.queues.get(mode);
    }

    @Override
    public void createQueue(FootMode mode) {
        this.queues.put(mode, new HashMap<>());
    }

    @Override
    public void deleteQueue(FootMode mode) {
        this.queues.remove(mode);
    }

    @Override
    public void processQueue(FootMode mode) {
        // Get the online arenas
        // If the number of arenas is limited, stop creating new ones
        ArrayList<Arena> onlineArenas = plugin.getArenaManager().getArenas();
        if(onlineArenas.size() >= maxArenas)
            return;

        // Sort the players by their MMR
        HashMap<Player, MatchmakingCriteria> spigotPlayerCriterias = this.queues.get(mode);
        HashMap<FootPlayer, MatchmakingCriteria> playerCriterias = new HashMap<>();
        spigotPlayerCriterias.forEach((player, criterias) -> {
            FootPlayer footPlayer = plugin.getPlayerService().getPlayerData(player);
            playerCriterias.put(footPlayer, criterias);
        });

        // If there is no min waiting players at least, cancel the function
        if(playerCriterias.size() < mode.getNumberOfSpawns()*2)
            return;

        ArrayList<FootPlayer> nonSortedList = new ArrayList<>(playerCriterias.keySet());
        ArrayList<ComparablePlayer> comparablePlayerList = new ArrayList<>();
        nonSortedList.forEach(player -> comparablePlayerList.add(new ComparablePlayer(player, mode)));
        Collections.sort(comparablePlayerList);

        ArrayList<FootPlayer> sortedPlayerList = new ArrayList<>();
        for (int i = 0; i < comparablePlayerList.size(); i++)
            sortedPlayerList.add(nonSortedList.get(i));

        if(sortedPlayerList.size() < mode.getNumberOfSpawns()*2)
            return;

        // If the minimum loop time isn't respected, remove player from eligible players
        new ArrayList<>(sortedPlayerList).forEach(player -> {
            MatchmakingCriteria criteria = playerCriterias.get(player);
            if(criteria.getCurrentLoopTime() < criteria.getMinimumLoopTime()) {
                sortedPlayerList.remove(player);
            }
            if(criteria.getCurrentLoopTime() != 0 && criteria.getCurrentLoopTime() % FootConfig.MATCHMAKING_LOOP_MMR_GAP_UPDATE == 0) {
                criteria.setMaxMmrGap(criteria.getMaxMmrGap() + FootConfig.MATCHMAKING_LOOP_MMR_GAP_UPDATE_INCREMENT);
                LOGGER.info("[Football] Queue: " + player.getOfflinePlayer().getName() + ": Increasing max mmr gap to " + criteria.getMaxMmrGap());
            }
            criteria.loop();
        });

        if(sortedPlayerList.size() < mode.getNumberOfSpawns()*2)
            return;

        // Check the online current mode arenas
        ArrayList<Arena> currentModeArenas = plugin.getArenaManager().getArenasByMode(mode);

        // Delete the ingame or full arenas
        new ArrayList<>(currentModeArenas).forEach(arena -> {
            if(arena.isPlaying() || arena.isFull())
                currentModeArenas.remove(arena);
        });

        // Get a random player into the list
        int playerId = new Random().nextInt(sortedPlayerList.size());
        ArrayList<FootPlayer> chosenPlayers = new ArrayList<>();
        FootPlayer mainPlayer = sortedPlayerList.get(playerId);
        chosenPlayers.add(mainPlayer);

        int gap = 1;
        while(gap < 10) {
            if(chosenPlayers.size() >= ((mode.getNumberOfSpawns()* FootTeamQualifier.values().length)+1)) {
                break;
            }
            if(sortedPlayerList.size() >= playerId + 1 + gap) {
                FootPlayer otherPlayerUp = sortedPlayerList.get(playerId + gap);
                if(compareGaps(mode, mainPlayer, playerCriterias.get(mainPlayer), otherPlayerUp, playerCriterias.get(otherPlayerUp)))
                    if(!chosenPlayers.contains(otherPlayerUp))
                        chosenPlayers.add(otherPlayerUp);
            }
            if(playerId > 0 && playerId - gap >= 0) {
                FootPlayer otherPlayerDown = sortedPlayerList.get(playerId - gap);
                if(compareGaps(mode, mainPlayer, playerCriterias.get(mainPlayer), otherPlayerDown, playerCriterias.get(otherPlayerDown)))
                    if(!chosenPlayers.contains(otherPlayerDown))
                        chosenPlayers.add(otherPlayerDown);
            }
            gap++;
        }

        if(chosenPlayers.size() < (mode.getNumberOfSpawns()*FootTeamQualifier.values().length)) {
            return;
        }

        // If there is no arenas, create new one and stop the function here
        switch (mode) {
            case CUSTOM:
                break;
            default:
                if(currentModeArenas.size() == 0) {
                    ArrayList<FootMap> modeMaps = Lists.newArrayList(plugin.getWorldService().getMaps(mode));
                    MinecraftWorld minecraftWorld = plugin.getWorldService().loadWorld(modeMaps.get(new Random().nextInt(modeMaps.size())), false);
                    CasualArena casualArena = new CasualArena(mode, minecraftWorld);
                    return;
                }
                break;
        }

        for(int i = 0; i < (mode.getNumberOfSpawns()* FootTeamQualifier.values().length); i++) {
            FootPlayer player = chosenPlayers.get(i);
            currentModeArenas.get(0).joinPlayer(player.getOfflinePlayer().getPlayer(), false);
            leaveMatchmaking(player.getOfflinePlayer().getPlayer());
        }

    }

    private boolean compareGaps(FootMode mode, FootPlayer player1, MatchmakingCriteria criteria1, FootPlayer player2, MatchmakingCriteria criteria2) {
        double gap = Math.abs(player1.getMmr().get(FootConfig.CURRENT_SEASON).get(mode) - player2.getMmr().get(FootConfig.CURRENT_SEASON).get(mode));
        return (gap <= criteria1.getMaxMmrGap()) && (gap <= criteria2.getMaxMmrGap());
    }

}
