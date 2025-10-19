package fr.codinbox.footballplugin.game;

import com.nametagedit.plugin.NametagEdit;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.language.LanguageKey;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.map.MinecraftWorld;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.action.PlayerExecutor;
import fr.codinbox.footballplugin.team.FootTeam;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CustomArena implements Arena {

    private static final Logger LOGGER = Bukkit.getLogger();

    private final PlayerExecutor playerExecutor;

    private final FootMode mode;
    private final FootMap map;
    private final MinecraftWorld minecraftWorld;

    private final UUID owner;
    private final String password;
    private final boolean allowSpectators;

    private GameState state;
    private Thread gameThread;
    private int gameTaskId;

    private Ball ball;
    private Thread ballThread;
    private int ballTaskId;

    private final HashMap<FootTeamQualifier, FootTeam> teams;
    private final ArrayList<OfflinePlayer> spectators;

    private HashMap<UUID, Integer> playerGoals;
    private HashMap<UUID, Integer> playerPasses;

    private int countdownTaskId;
    private boolean isCountdown;

    private Integer startTimer;
    private Integer gameTimer;
    private int totalGameTime;

    private boolean overtime;
    private boolean shutdown;

    private GameBossBar gameBossBar;

    public CustomArena(FootMode mode, MinecraftWorld minecraftWorld, UUID owner, String password, boolean allowSpectators) {
        this.playerExecutor = new PlayerExecutor(this);

        this.mode = mode;
        this.map = minecraftWorld.getParentMap();
        this.minecraftWorld = minecraftWorld;

        this.owner = owner;
        this.password = password;
        this.allowSpectators = allowSpectators;

        // Creating teams and spawns
        this.teams = new HashMap<>();

        for(Map.Entry<FootTeamQualifier, ArrayList<SerializablePosition>> teamSpawns : map.getTeamSpawns().entrySet())
            this.teams.put(teamSpawns.getKey(), new FootTeam(teamSpawns.getKey(), new ArrayList<>(), teamSpawns.getValue()));

        this.spectators = new ArrayList<>();

        // Spawn the ball
        this.ball = FootballPlugin.INSTANCE.getBallManager().spawnBall(this.map.getCenterLocation(this.minecraftWorld.getWorld()));
        centerBall();
        startBallThread();

        initWaitingState();

        // Registering arena into ArenaManager
        FootballPlugin.INSTANCE.getArenaManager().registerArena(this);

        this.playerGoals = new HashMap<>();
        this.playerPasses = new HashMap<>();
        this.isCountdown = false;
        this.overtime = false;
        this.shutdown = false;
        this.gameBossBar = new GameBossBar(this);
    }

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {
        return this.minecraftWorld.getName();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAllowingSpectators() {
        return this.allowSpectators;
    }

    @Override
    public PlayerExecutor getPlayerExecutor() {
        return this.playerExecutor;
    }

    /**
     * Join a player into the arena
     * @param player Player to join
     * @param spectator Ignored here, team selection will be available
     */
    @Override
    public void joinPlayer(Player player, boolean spectator) {
        joinSpectator(player);
        if(this.owner.equals(player.getUniqueId()))
            player.sendMessage(new LanguageManager.Phrase(LanguageKey.CUSTOM_ADMIN_MESSAGE).toString());
        FootballPlugin.INSTANCE.getArenaManager().reportPlayerArena(player, this);
    }

    public void joinTeam(Player player, FootTeamQualifier qualifier) {
        if(teams.get(qualifier).isFull())
            return;

        FootTeam team = getPlayerTeam(player);
        if(team != null) {
            gameBossBar.removeBossBar(player);
            team.getPlayersUuid().remove(player.getUniqueId());
        }

        this.spectators.remove(player);

        this.teams.get(qualifier).getPlayersUuid().add(player.getUniqueId());

        // Player is not spectating the match
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);
        teleportPlayerToSpawn(player);

        NametagEdit.getApi().setPrefix(player, qualifier.getColorCode().replace("§", "&"));

        playerExecutor.broadcastMessage(new LanguageManager.Phrase(LanguageKey.ARENA_PLAYER_JOIN_NO_COUNT)
                .replaceVar("name", ChatColor.stripColor(player.getName()))
                .toString());

        gameBossBar.createBossBar(player);

        if(!playerGoals.containsKey(player.getUniqueId()))
            this.playerGoals.put(player.getUniqueId(), 0);
        if(!playerPasses.containsKey(player.getUniqueId()))
            this.playerPasses.put(player.getUniqueId(), 0);
    }

    public void joinSpectator(Player player) {
        this.spectators.add(player);

        NametagEdit.getApi().setPrefix(player, "&7");
        FootballPlugin.INSTANCE.getFrozePlayers().remove(player);

        FootTeam team = getPlayerTeam(player);
        if(team != null) {
            team.getPlayersUuid().remove(player.getUniqueId());
            gameBossBar.removeBossBar(player);
        }

        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(this.map.getCenterLocation(this.minecraftWorld.getWorld()));

        createScoreboard(player);

        playerExecutor.broadcastMessage(new LanguageManager.Phrase(LanguageKey.ARENA_PLAYER_SPECTATOR_JOIN)
                .replaceVar("name", ChatColor.stripColor(player.getName()))
                .toString());

        player.sendMessage(new LanguageManager.Phrase(LanguageKey.TEAM_SELECT_MESSAGE).toString());
    }

    @Override
    public void leavePlayer(OfflinePlayer player) {
        if(this.owner.equals(player.getUniqueId()) && !shutdown) {
            shutdown = true;
            endMatch();
        }

        this.spectators.remove(player);

        FootballPlugin.INSTANCE.getFrozePlayers().remove(player);

        if(this.mode.equals(FootMode.CUSTOM) || this.state.equals(GameState.ENDING)) {
            if(getPlayerTeam(player) != null)
                this.teams.get(getPlayerTeam(player).getQualifier()).getPlayersUuid().remove(player.getUniqueId());
            FootballPlugin.INSTANCE.getArenaManager().reportPlayerLeaveArena(player);
        }
        switch (this.state) {
            case WAITING:
            case STARTING:
                playerExecutor.broadcastMessage(new LanguageManager.Phrase(LanguageKey.ARENA_PLAYER_LEAVE_NO_COUNT)
                    .replaceVar("name", ChatColor.stripColor(player.getName()))
                    .toString());
                break;
        }
        if(player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            NametagEdit.getApi().clearNametag(onlinePlayer);
            destroyScoreboard(onlinePlayer);
            gameBossBar.removeBossBar(onlinePlayer);
            onlinePlayer.teleport(Bukkit.getWorld("world").getSpawnLocation());
            onlinePlayer.setGameMode(GameMode.SURVIVAL);
        }
    }

    @Override
    public void setTeam(Player player, FootTeam team) {

    }

    @Override
    public FootTeam getTeamByQualifier(FootTeamQualifier qualifier) {
        return this.teams.get(qualifier);
    }

    @Override
    public Collection<FootTeam> getTeams() {
        return this.teams.values();
    }

    @Override
    public ArrayList<OfflinePlayer> getIngamePlayers() {
        ArrayList<OfflinePlayer> players = new ArrayList<>();
        for(FootTeam team : this.teams.values()) {
            players.addAll(team.getPlayers());
        }
        return players;
    }

    @Override
    public ArrayList<OfflinePlayer> getSpectators() {
        return this.spectators;
    }

    @Override
    public ArrayList<OfflinePlayer> getAllPlayers() {
        ArrayList<OfflinePlayer> array = new ArrayList<>(getIngamePlayers());
        array.addAll(getSpectators());
        return array;
    }

    @Override
    public FootTeam getPlayerTeam(OfflinePlayer player) {
        for(Map.Entry<FootTeamQualifier, FootTeam> team : this.teams.entrySet()) {
            if (team.getValue().hasPlayer(player.getUniqueId()))
                return team.getValue();
        }
        return null;
    }

    @Override
    public boolean hasPlayer(OfflinePlayer player) {
        boolean hasPlayer = false;
        for (OfflinePlayer ingamePlayer : getIngamePlayers()) {
            if(player.getUniqueId().toString().equals(ingamePlayer.getUniqueId().toString())) {
                hasPlayer = true;
                break;
            }
        }
        return hasPlayer;
    }

    @Override
    public void setState(GameState state) {
        endCurrentState();
        switch (state) {
            case WAITING:
                initWaitingState();
                break;

            case STARTING:
                initStartingState();
                break;

            case INGAME:
                initIngameState();
                break;
            case ENDING:
                this.state = GameState.ENDING;
        }
    }

    private void endCurrentState() {
        Bukkit.getScheduler().cancelTask(this.gameTaskId);
        this.gameThread.interrupt();
    }

    private void initWaitingState() {
        this.state = GameState.WAITING;

        if(this.gameThread == null || !this.gameThread.isAlive())
            this.gameThread = new Thread(() -> {
                gameTaskId = startWaitingTask();
            });

        // Starting waiting phase thread
        gameThread.start();
    }

    private void initStartingState() {
        this.state = GameState.STARTING;

        if(this.gameThread == null || !this.gameThread.isAlive())
            this.gameThread = new Thread(() -> {
                gameTaskId = startStartingTask();
            });

        // Starting starting phase thread
        this.gameThread.start();
    }

    private void initIngameState() {
        this.state = GameState.INGAME;

        if(this.gameThread == null || !this.gameThread.isAlive())
            this.gameThread = new Thread(() -> {
                gameTaskId = startGameTask();
            });

        gameBossBar.updateBossBars();

        centerAndCountdown();

        // Starting starting phase thread
        this.gameThread.start();
    }

    @Override
    public GameState getCurrentState() {
        return this.state;
    }

    @Override
    public boolean isPlaying() {
        return this.state != GameState.WAITING;
    }

    @Override
    public boolean isFull() {
        return getIngamePlayers().size() >= mode.getNumberOfSpawns() * FootTeamQualifier.values().length
                && this.spectators.size() >= FootConfig.GAME_MAX_SPECTATORS;
    }

    @Override
    public MinecraftWorld getMap() {
        return this.minecraftWorld;
    }

    @Override
    public FootMode getMode() {
        return this.mode;
    }

    @Override
    public void startMatch() {
        if(!GameState.WAITING.equals(this.state))
            return;

        setState(GameState.STARTING);
    }

    @Override
    public Ball getBall() {
        return this.ball;
    }

    @Override
    public void centerBall() {
        this.ball.setVelocity(new Vector(0f, 0f, 0f));
        this.ball.teleport(this.map.getCenterLocation(this.minecraftWorld.getWorld()));
    }

    @Override
    public void goal(FootTeam scoredTeam, Location location) {
        TimeEntry<UUID> lastBallHitEntry = ball.getLastHit();
        TimeEntry<UUID> lastBallPassHitEntry = ball.getPassHit();

        FootTeam scoreTeam = this.teams.get(scoredTeam.getQualifier().getOtherTeam());

        if(GameState.INGAME.equals(this.state) && lastBallHitEntry != null) {

            Player scorePlayer = Bukkit.getPlayer(lastBallHitEntry.getValue());

            scoreTeam.setGoals(scoreTeam.getGoals() + 1);

            FootTeamQualifier scoreTeamQualifier = scoreTeam.getQualifier();
            FootTeamQualifier scoredTeamQualifier = scoredTeam.getQualifier();

            playerExecutor.broadcastMessage(new LanguageManager.Phrase(LanguageKey.GOAL_SCORED)
                    .replaceVar("playerteamcolor", getPlayerTeam(scorePlayer).getQualifier().getColorCode())

                    .replaceVar("scoreteamcolor", scoreTeamQualifier.getColorCode())
                    .replaceVar("scoreteamname", scoreTeamQualifier.getLanguageName())
                    .replaceVar("scoreteamgoals", String.valueOf(scoreTeam.getGoals()))

                    .replaceVar("scoredteamcolor", scoredTeamQualifier.getColorCode())
                    .replaceVar("scoredteamname", scoredTeamQualifier.getLanguageName())
                    .replaceVar("scoredteamgoals", String.valueOf(scoredTeam.getGoals()))

                    .replaceVar("playername", ChatColor.stripColor(scorePlayer.getName()))
                    .toString());

            if(!scoredTeam.hasPlayer(scorePlayer.getUniqueId()))
                this.playerGoals.put(scorePlayer.getUniqueId(), this.playerGoals.get(scorePlayer.getUniqueId()) + 1);

            if(lastBallPassHitEntry != null) {
                if(!lastBallPassHitEntry.getValue().equals(lastBallHitEntry.getValue())) {
                    Player passingPlayer = Bukkit.getPlayer(lastBallPassHitEntry.getValue());
                    long ballHitTime = lastBallHitEntry.getTime();
                    long ballPassTime = lastBallPassHitEntry.getTime();
                    long totalTravelTime = ballHitTime - ballPassTime;

                    if(scoreTeam.equals(getPlayerTeam(passingPlayer)) && totalTravelTime <= FootConfig.MAX_ASSIST_TIME) {
                        playerExecutor.broadcastMessage(new LanguageManager.Phrase(LanguageKey.GOAL_ASSIST)
                                .replaceVar("playername", ChatColor.stripColor(passingPlayer.getName()))
                                .toString());
                        if(!scoredTeam.hasPlayer(passingPlayer.getUniqueId()))
                            this.playerPasses.put(passingPlayer.getUniqueId(), this.playerPasses.get(passingPlayer.getUniqueId()) + 1);
                    }
                }
            }

            this.ball.resetHits();
            gameBossBar.updateBossBars();

            if(overtime)
                endMatch();
            else
                centerAndCountdown();

        }

        minecraftWorld.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);
        centerBall();
    }

    private void centerAndCountdown() {
        this.isCountdown = true;

        centerBall();

        HashMap<Player, Location> playerSpawnMap = playerSpawnMap();
        ArrayList<OfflinePlayer> gamePlayers = getIngamePlayers();
        gamePlayers.forEach(player -> {
            player.getPlayer().teleport(playerSpawnMap.get(player.getPlayer()));
            FootballPlugin.INSTANCE.getFrozePlayers().put(player.getPlayer(), new SerializableLocation(playerSpawnMap.get(player.getPlayer())));
        });

        this.countdownTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FootballPlugin.INSTANCE, new Runnable() {

            private int timer = 5;
            private float pitch = 1f;

            @Override
            public void run() {
                if(timer <= 3) {
                    if(timer > 0) {
                        playerExecutor.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1f, pitch);
                        playerExecutor.broadcastMessage(new LanguageManager.Phrase(LanguageKey.ROUND_STARTING_COUNTDOWN)
                                .replaceVar("seconds", String.valueOf(timer))
                                .toString());
                    }
                    switch (timer) {
                        case 3:
                            gamePlayers.forEach(player -> Title.sendTitle(player.getPlayer(), "§c" + timer, 1, 1, 18));
                            break;
                        case 2:
                            gamePlayers.forEach(player -> Title.sendTitle(player.getPlayer(), "§6" + timer, 1, 1, 18));
                            break;
                        case 1:
                            gamePlayers.forEach(player -> Title.sendTitle(player.getPlayer(), "§a" + timer, 1, 1, 18));
                            break;
                    }
                    if(timer == 0) {
                        for(OfflinePlayer player : gamePlayers) {
                            Player onlinePlayer = player.getPlayer();
                            Title.sendTitle(onlinePlayer, new LanguageManager.Phrase(LanguageKey.GO).toString(), 1, 1, 1);
                            playerExecutor.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        }
                        centerAndCountDownEnd();
                        Bukkit.getScheduler().cancelTask(countdownTaskId);
                    }
                    pitch += 0.5f;
                }
                timer--;
            }
        }, 0, 20);

    }

    private void centerAndCountDownEnd() {
        for(OfflinePlayer player : getIngamePlayers()) {
            FootballPlugin.INSTANCE.getFrozePlayers().remove(player.getPlayer());
        }
        this.isCountdown = false;
    }

    private void startBallThread() {
        this.ballThread = new Thread(() -> {

            ballTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(FootballPlugin.INSTANCE, () -> {

                Location ballLocation = ball.getLocation();
                Location ballBlockLocation = ball.getLocation().clone();
                ballBlockLocation.setY(1);
                Block bottomBlock = ballBlockLocation.getBlock();

                for(FootTeamQualifier qualifier : this.teams.keySet()) {
                    FootTeam team = this.teams.get(qualifier);
                    if(bottomBlock.getType().equals(qualifier.getItem().getType())) {
                        goal(team, ballLocation);
                        break;
                    }
                }

            }, 0, 5);

        });

        this.ballThread.start();
    }

    private void stopBallThread() {
        Bukkit.getScheduler().cancelTask(this.ballTaskId);
        if(ballThread != null)
            ballThread.interrupt();
    }

    @Override
    public void endMatch() {
        switch (this.state) {
            case WAITING:
            case STARTING:
            case ENDING:
                for(OfflinePlayer player : getAllPlayers()) {
                    if(player.isOnline())
                        leavePlayer(player.getPlayer());
                }
                destroy();
                break;
            case INGAME:
                if(isCountdown) {
                    Bukkit.getScheduler().cancelTask(this.countdownTaskId);
                    centerAndCountDownEnd();
                }
                Bukkit.getScheduler().cancelTask(this.gameTaskId);
                this.state = GameState.ENDING;
                for (OfflinePlayer player : getAllPlayers()) {
                    if(getIngamePlayers().contains(player))
                        displayEndSummary(player.getPlayer());
                }
                destroy();
                break;
        }
    }

    @Override
    public void destroy() {
        stopBallThread();
        gameBossBar.destroy();
        Bukkit.getScheduler().cancelTask(this.gameTaskId);
        gameBossBar.destroy();
        if(gameThread != null)
            gameThread.interrupt();

        if(!shutdown) {

            try {
                Bukkit.getScheduler().runTaskLater(FootballPlugin.INSTANCE, () -> {
                    CustomArena newArena = new CustomArena(mode, FootballPlugin.INSTANCE.getWorldService().loadWorld(map, false), owner, password, allowSpectators);

                    for (OfflinePlayer player : getAllPlayers()) {
                        if (!player.isOnline())
                            continue;
                        newArena.joinPlayer(player.getPlayer(), false);
                    }

                    FootballPlugin.INSTANCE.getWorldService().unloadWorld(this.minecraftWorld);
                    FootballPlugin.INSTANCE.getArenaManager().unregisterArena(this);
                }, 5 * 20);
            }
            catch (IllegalPluginAccessException exception) {
                FootballPlugin.INSTANCE.getWorldService().unloadWorld(this.minecraftWorld);
                FootballPlugin.INSTANCE.getArenaManager().unregisterArena(this);
            }
        }
        else {
            FootballPlugin.INSTANCE.getWorldService().unloadWorld(this.minecraftWorld);
            FootballPlugin.INSTANCE.getArenaManager().unregisterArena(this);
        }
    }

    @Override
    public HashMap<Player, Location> playerSpawnMap() {
        HashMap<Player, Integer> spawns = playerSpawnSecondMap();
        HashMap<Player, Location> finalSpawns = new HashMap<>();

        ArrayList<AttribuedPlayerSpawn> blueSpawns = new ArrayList<>();
        spawns.forEach((player, spawnId) -> {
            if(getPlayerTeam(player) != null && getPlayerTeam(player).getQualifier().equals(FootTeamQualifier.BLUE)) {
                blueSpawns.add(new AttribuedPlayerSpawn(player, spawnId));
            }
        });
        Collections.sort(blueSpawns);

        ArrayList<AttribuedPlayerSpawn> redSpawns = new ArrayList<>();
        spawns.forEach((player, spawnId) -> {
            if(getPlayerTeam(player) != null && getPlayerTeam(player).getQualifier().equals(FootTeamQualifier.RED)) {
                redSpawns.add(new AttribuedPlayerSpawn(player, spawnId));
            }
        });
        Collections.sort(redSpawns);

        if(blueSpawns.size() >= redSpawns.size()) {
            for(int a = 0; a < blueSpawns.size(); a++) {
                AttribuedPlayerSpawn bluePlayer = blueSpawns.get(a);
                AttribuedPlayerSpawn redPlayer;
                try {
                    redPlayer = redSpawns.get(a);
                }
                catch (IndexOutOfBoundsException e) {
                    redPlayer = null;
                }
                finalSpawns.put(bluePlayer.player, teams.get(FootTeamQualifier.BLUE).getSpawns().get(bluePlayer.spawnId).toLocation(minecraftWorld.getWorld()));
                if(redPlayer != null)
                    finalSpawns.put(redPlayer.player, teams.get(FootTeamQualifier.RED).getSpawns().get(bluePlayer.spawnId).toLocation(minecraftWorld.getWorld()));
            }
        }
        else {
            for(int a = 0; a < redSpawns.size(); a++) {
                AttribuedPlayerSpawn redPlayer = redSpawns.get(a);
                AttribuedPlayerSpawn bluePlayer;
                try {
                    bluePlayer = blueSpawns.get(a);
                }
                catch (IndexOutOfBoundsException e) {
                    bluePlayer = null;
                }
                finalSpawns.put(redPlayer.player, teams.get(FootTeamQualifier.RED).getSpawns().get(redPlayer.spawnId).toLocation(minecraftWorld.getWorld()));
                if(bluePlayer != null)
                    finalSpawns.put(bluePlayer.player, teams.get(FootTeamQualifier.BLUE).getSpawns().get(redPlayer.spawnId).toLocation(minecraftWorld.getWorld()));
            }
        }

        return finalSpawns;
    }

    private HashMap<Player, Integer> playerSpawnSecondMap() {
        HashMap<Player, Integer> spawnMap = new HashMap<>();
        for(FootTeamQualifier teamQualifier : this.teams.keySet()) {
            FootTeam team = this.teams.get(teamQualifier);
            ArrayList<SerializablePosition> teamSpawnLocations = team.getSpawns();
            ArrayList<SerializablePosition> usedSpawns = new ArrayList<>();
            for(OfflinePlayer player : team.getPlayers()) {
                if(!player.isOnline())
                    continue;

                Random random = new Random();

                int spawnId = random.nextInt(teamSpawnLocations.size());
                while(usedSpawns.contains(teamSpawnLocations.get(spawnId))) {
                    spawnId = random.nextInt(teamSpawnLocations.size());
                }

                SerializablePosition location = teamSpawnLocations.get(spawnId);
                usedSpawns.add(location);

                spawnMap.put(player.getPlayer(), spawnId);
            }
        }
        return spawnMap;
    }

    private void teleportPlayersToSpawns() {
        for(Map.Entry<Player, Location> spawn : playerSpawnMap().entrySet())
            spawn.getKey().teleport(spawn.getValue());
    }

    @Override
    public Location teleportPlayerToSpawn(Player player) {
        Location location = playerSpawnMap().get(player);
        if(location == null)
            location = getPlayerTeam(player).getSpawns().get(0).toLocation(minecraftWorld.getWorld());
        player.teleport(location);
        if(isCountdown) {
            FootballPlugin.INSTANCE.getFrozePlayers().put(player, new SerializableLocation(location));
        }
        return location;
    }

    private void createScoreboard(Player player) {
        if(FootballPlugin.INSTANCE.getScoreboards().containsKey(player))
            FootballPlugin.INSTANCE.getScoreboards().get(player).destroy();
        ScoreboardSign scoreboardSign = new ScoreboardSign(player, "§b§l" + mode.getModeName() + (mode.getSubModeName() != null ? " " + mode.getSubModeName() : ""));
        scoreboardSign.create();
        scoreboardSign.setLine(0, "§f");

        if(getPlayerTeam(player) != null) {
            FootTeam team = getPlayerTeam(player);
            FootTeamQualifier teamQualifier = team.getQualifier();

            FootTeamQualifier otherTeamQualifier = teamQualifier.getOtherTeam();
            FootTeam otherTeam = this.teams.get(otherTeamQualifier);

            // Player isn't spectator
            switch (this.state) {
                case WAITING:
                    scoreboardSign.setLine(1, "§fMap: §a" + this.map.getName());
                    scoreboardSign.setLine(2, "§b");
                    scoreboardSign.setLine(3, "§fStarting in §c--s");
                    break;
                case INGAME:
                    scoreboardSign.setLine(1, new LanguageManager.Phrase(LanguageKey.BOSS_GOALS)
                            .replaceVar("currentteamcolor", teamQualifier.getColorCode())
                            .replaceVar("currentteamname", teamQualifier.getLanguageName())
                            .replaceVar("currentteamgoals", String.valueOf(team.getGoals()))
                            .replaceVar("enemyteamcolor", otherTeamQualifier.getColorCode())
                            .replaceVar("enemyteamname", otherTeamQualifier.getLanguageName())
                            .replaceVar("enemyteamgoals", String.valueOf(otherTeam.getGoals()))
                            .toString());
                    scoreboardSign.setLine(2, "§b");
                    scoreboardSign.setLine(3, "§fGoals: §a" + this.playerGoals.get(player.getUniqueId()));
                    scoreboardSign.setLine(4, "§fAssists: §a" + this.playerPasses.get(player.getUniqueId()));
                    scoreboardSign.setLine(5, "§d");
                    scoreboardSign.setLine(6, "§fTime remaining: §e--:--");
                    break;
            }
        }
        else {
            // Player is spectator
            switch (this.state) {
                case WAITING:
                    scoreboardSign.setLine(1, "§fMap: §a" + this.map.getName());
                    scoreboardSign.setLine(2, "§b");
                    scoreboardSign.setLine(3, "§fStarting in §c--s");
                    break;
                case INGAME:
                    scoreboardSign.setLine(1, new LanguageManager.Phrase(LanguageKey.BOSS_GOALS)
                            .replaceVar("currentteamcolor", FootTeamQualifier.BLUE.getColorCode())
                            .replaceVar("currentteamname", FootTeamQualifier.BLUE.getLanguageName())
                            .replaceVar("currentteamgoals", String.valueOf(teams.get(FootTeamQualifier.BLUE).getGoals()))
                            .replaceVar("enemyteamcolor", FootTeamQualifier.RED.getColorCode())
                            .replaceVar("enemyteamname", FootTeamQualifier.RED.getLanguageName())
                            .replaceVar("enemyteamgoals", String.valueOf(teams.get(FootTeamQualifier.RED).getGoals()))
                            .toString());
                    scoreboardSign.setLine(2, "§b");
                    scoreboardSign.setLine(3, "§fTime remaining: §e--:--");
                    break;
            }
        }
        FootballPlugin.INSTANCE.getScoreboards().put(player, scoreboardSign);
    }

    private void destroyScoreboard(Player player) {
        if(FootballPlugin.INSTANCE.getScoreboards().containsKey(player)) {
            FootballPlugin.INSTANCE.getScoreboards().get(player).destroy();
            FootballPlugin.INSTANCE.getScoreboards().remove(player);
        }
    }

    private void updateScoreboard(Player player) {
        if(!FootballPlugin.INSTANCE.getScoreboards().containsKey(player))
            return;

        ScoreboardSign scoreboardSign = FootballPlugin.INSTANCE.getScoreboards().get(player);

        FootTeam team = getPlayerTeam(player);
        if(team != null) {
            // Player isn't spectator
            FootTeamQualifier teamQualifier = team.getQualifier();

            FootTeamQualifier otherTeamQualifier = teamQualifier.getOtherTeam();
            FootTeam otherTeam = this.teams.get(otherTeamQualifier);
            switch (this.state) {
                case STARTING:
                    if(this.startTimer != null)
                        scoreboardSign.setLine(3, "§fStarting in §a" + this.startTimer  + "s");
                    break;
                case INGAME:
                    scoreboardSign.setLine(1, new LanguageManager.Phrase(LanguageKey.BOSS_GOALS)
                            .replaceVar("currentteamcolor", teamQualifier.getColorCode())
                            .replaceVar("currentteamname", teamQualifier.getLanguageName())
                            .replaceVar("currentteamgoals", String.valueOf(team.getGoals()))
                            .replaceVar("enemyteamcolor", otherTeamQualifier.getColorCode())
                            .replaceVar("enemyteamname", otherTeamQualifier.getLanguageName())
                            .replaceVar("enemyteamgoals", String.valueOf(otherTeam.getGoals()))
                            .toString());
                    scoreboardSign.setLine(2, "§b");
                    scoreboardSign.setLine(3, "§fGoals: §a" + this.playerGoals.get(player.getUniqueId()));
                    scoreboardSign.setLine(4, "§fAssists: §a" + this.playerPasses.get(player.getUniqueId()));
                    scoreboardSign.setLine(5, "§d");
                    if(this.gameTimer != null) {
                        int timerSeconds = (int) (this.gameTimer - (TimeUnit.SECONDS.toMinutes(this.gameTimer) * 60));
                        String timerSecondsStr = String.valueOf(timerSeconds).length() == 1 ? "0" + timerSeconds : String.valueOf(timerSeconds);
                        if(!this.overtime)
                            scoreboardSign.setLine(6, "§fTime remaining: §e" + TimeUnit.SECONDS.toMinutes(this.gameTimer) + ":" + timerSecondsStr);
                        else
                            scoreboardSign.setLine(6, "§fOVERTIME: §e" + TimeUnit.SECONDS.toMinutes(this.gameTimer) + ":" + timerSecondsStr);
                    }
                    break;
                }
        }
        else {
            // Player is spectator
            switch (this.state) {
                case STARTING:
                    if(this.startTimer != null)
                        scoreboardSign.setLine(3, "§fStarting in §a" + this.startTimer  + "s");
                    break;
                case INGAME:
                    scoreboardSign.setLine(1, new LanguageManager.Phrase(LanguageKey.BOSS_GOALS)
                            .replaceVar("currentteamcolor", FootTeamQualifier.BLUE.getColorCode())
                            .replaceVar("currentteamname", FootTeamQualifier.BLUE.getLanguageName())
                            .replaceVar("currentteamgoals", String.valueOf(teams.get(FootTeamQualifier.BLUE).getGoals()))
                            .replaceVar("enemyteamcolor", FootTeamQualifier.RED.getColorCode())
                            .replaceVar("enemyteamname", FootTeamQualifier.RED.getLanguageName())
                            .replaceVar("enemyteamgoals", String.valueOf(teams.get(FootTeamQualifier.RED).getGoals()))
                            .toString());
                    scoreboardSign.setLine(2, "§b");
                    int timerSeconds = (int) (this.gameTimer - (TimeUnit.SECONDS.toMinutes(this.gameTimer) * 60));
                    String timerSecondsStr = String.valueOf(timerSeconds).length() == 1 ? "0" + timerSeconds : String.valueOf(timerSeconds);
                    if(!this.overtime)
                        scoreboardSign.setLine(3, "§fTime remaining: §e" + TimeUnit.SECONDS.toMinutes(this.gameTimer) + ":" + timerSecondsStr);
                    else
                        scoreboardSign.setLine(3, "§fOVERTIME: §e" + TimeUnit.SECONDS.toMinutes(this.gameTimer) + ":" + timerSecondsStr);
                    break;
            }
        }
    }

    private void updateScoreboards() {
        for(OfflinePlayer player : getAllPlayers())
            updateScoreboard(player.getPlayer());
    }

    private ArrayList<SerializablePosition> getAllSpawns() {
        ArrayList<SerializablePosition> spawns = new ArrayList<>();
        for(FootTeam team : this.teams.values())
            spawns.addAll(team.getSpawns());
        return spawns;
    }

    private SerializablePosition getRandomSpawn() {
        ArrayList<SerializablePosition> spawns = getAllSpawns();
        if(spawns.size() == 0)
            return null;
        return getAllSpawns().get(new Random().nextInt(spawns.size() - 1));
    }

    private int startWaitingTask() {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(FootballPlugin.INSTANCE, () -> {

            ActionBar bar = new ActionBar(getAllPlayers(), new LanguageManager.Phrase(LanguageKey.ACTIONBAR_WAITING_PLAYERS_NO_COUNT)
                .toString());

            bar.send();
        }, 0, 20);
    }

    private int startStartingTask() {
        startTimer = FootConfig.GAME_START_TIMER;

        return Bukkit.getScheduler().scheduleSyncRepeatingTask(FootballPlugin.INSTANCE, () -> {

            PlayerExecutor.PlayerUnit playerUnit = playerExecutor.multipleAction();

            if(startTimer == 30
                || startTimer == 15
                || startTimer == 10
                || (startTimer <= 5 && startTimer > 0)) {
                LanguageManager.Phrase countDownPhrase = new LanguageManager.Phrase(LanguageKey.GAME_STARTING_COUNTDOWN)
                        .replaceVar("seconds", String.valueOf(startTimer));
                playerUnit
                        .addExecutor(executor -> {
                            executor.sendMessage(countDownPhrase.toString());
                            executor.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        });
            }
            else if(startTimer == 0)
                setState(GameState.INGAME);

            updateScoreboards();

            playerUnit.addExecutor(actionExecutor -> actionExecutor.setLevel(startTimer));
            playerUnit.executeActions();

            startTimer--;

        }, 0, 20);
    }

    private int startGameTask() {
        this.gameTimer = FootConfig.GAME_GAME_TIMER;
        this.totalGameTime = 0;
        this.overtime = false;

        getAllPlayers().forEach(player -> createScoreboard(player.getPlayer()));

        return Bukkit.getScheduler().scheduleSyncRepeatingTask(FootballPlugin.INSTANCE, () -> {

            updateScoreboards();

            if (gameTimer == 60
                    || gameTimer == 30
                    || (gameTimer <= 3 && gameTimer > 0)) {
                if(!isCountdown) {
                    playerExecutor.multipleAction()
                            .addExecutor(executor -> {
                                if (gameTimer == 60)
                                    if (!overtime)
                                        executor.sendMessage(new LanguageManager.Phrase(LanguageKey.GAME_MINUTE_ALERT)
                                                .replaceVar("time", String.valueOf(TimeUnit.SECONDS.toMinutes(gameTimer)))
                                                .toString());
                                    else
                                        executor.sendMessage(new LanguageManager.Phrase(LanguageKey.GAME_ENDING_MINUTES_ALERT)
                                                .replaceVar("time", String.valueOf(TimeUnit.SECONDS.toMinutes(gameTimer)))
                                                .toString());
                                else if (!overtime)
                                    executor.sendMessage(new LanguageManager.Phrase(LanguageKey.GAME_SECONDS_ALERT)
                                            .replaceVar("time", String.valueOf(gameTimer))
                                            .toString());
                                else
                                    executor.sendMessage(new LanguageManager.Phrase(LanguageKey.GAME_ENDING_SECONDS_ALERT)
                                            .replaceVar("time", String.valueOf(gameTimer))
                                            .toString());
                                executor.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                            }).executeActions();
                }
            }

            if(gameTimer == 0) {
                if(teams.get(FootTeamQualifier.BLUE).getGoals() == teams.get(FootTeamQualifier.RED).getGoals() && !overtime) {
                        overtime = true;
                        gameTimer = FootConfig.GAME_OVERTIME_TIMER;
                        int timerSeconds = (int) (gameTimer - (TimeUnit.SECONDS.toMinutes(gameTimer) * 60));
                        String timerSecondsStr = String.valueOf(timerSeconds).length() == 1 ? "0" + timerSeconds : String.valueOf(timerSeconds);
                        centerAndCountdown();
                        playerExecutor.multipleAction().addExecutor(executor -> {
                            executor.sendMessage(new LanguageManager.Phrase(LanguageKey.GAME_OVERTIME_MESSAGE)
                                    .replaceVar("minutes", String.valueOf(TimeUnit.SECONDS.toMinutes(gameTimer)))
                                    .replaceVar("seconds", timerSecondsStr)
                                    .toString());
                            executor.playSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                        }).executeActions();
                    }
                else {
                    endMatch();
                }
            }

            if(!isCountdown) {
                gameTimer--;
                totalGameTime++;
            }
        }, 0, 20);
    }

    private void displayEndSummary(Player player) {
        int timerSeconds = (int) (totalGameTime - (TimeUnit.SECONDS.toMinutes(totalGameTime) * 60));
        String timerSecondsStr = String.valueOf(timerSeconds).length() == 1 ? "0" + timerSeconds : String.valueOf(timerSeconds);
        player.sendMessage("§a§m───────────────────────────────────");
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player, "§b§l" + this.mode.getModeName() + " " + (this.mode.getSubModeName() != null ? this.mode.getSubModeName() : "") + " §8(Game duration: " + TimeUnit.SECONDS.toMinutes(totalGameTime) + ":" + timerSecondsStr + ")");
        player.sendMessage("");
        ChatUtils.sendCenteredMessage(player, "§fTotal goals: §a" + this.playerGoals.get(player.getUniqueId()) + " §f§l| §fTotal assists: §a" + this.playerPasses.get(player.getUniqueId()));
        player.sendMessage("");
        player.sendMessage("§a§m───────────────────────────────────");
    }

    private class AttribuedPlayerSpawn implements Comparable<AttribuedPlayerSpawn> {

        public Player player;
        public int spawnId;

        public AttribuedPlayerSpawn(Player player, int spawnId) {
            this.player = player;
            this.spawnId = spawnId;
        }

        @Override
        public String toString() {
            return player.toString() + " WITH SPAWN " + spawnId;
        }

        @Override
        public int compareTo(AttribuedPlayerSpawn o) {
            return Integer.compare(spawnId, o.spawnId);
        }
    }

}
