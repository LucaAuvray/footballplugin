package fr.codinbox.footballplugin;

import fr.codinbox.footballplugin.commands.FootballCommand;
import fr.codinbox.footballplugin.configuration.ConfigLoader;
import fr.codinbox.footballplugin.game.ArenaManager;
import fr.codinbox.footballplugin.game.BallManager;
import fr.codinbox.footballplugin.inventory.InventoryServiceImpl;
import fr.codinbox.footballplugin.language.LanguageManager;
import fr.codinbox.footballplugin.listeners.BallListener;
import fr.codinbox.footballplugin.listeners.InventoryListener;
import fr.codinbox.footballplugin.listeners.PlayerListener;
import fr.codinbox.footballplugin.listeners.WorldListener;
import fr.codinbox.footballplugin.map.MinecraftWorldServiceImpl;
import fr.codinbox.footballplugin.ranking.RankingServiceImpl;
import fr.codinbox.footballplugin.service.*;
import fr.codinbox.footballplugin.matchmaking.MatchmakingServiceImpl;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.FootPlayerServiceImpl;
import fr.codinbox.footballplugin.team.FootTeamQualifier;
import fr.codinbox.footballplugin.utils.ScoreboardSign;
import fr.codinbox.footballplugin.utils.SerializableLocation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FootballPlugin extends JavaPlugin {

    private static final Logger LOGGER = Bukkit.getLogger();

    public static FootballPlugin INSTANCE;

    private File dataDirectory;
    private File worldsDirectory;

    private LanguageManager languageManager;
    private BallManager ballManager;
    private ArenaManager arenaManager;

    private MinecraftWorldService worldService;
    private FootPlayerService playerService;
    private InventoryService inventoryService;
    private MatchmakingService matchmakingService;
    private RankingService rankingService;

    private HashMap<OfflinePlayer, SerializableLocation> frozePlayers;
    private HashMap<Player, ScoreboardSign> scoreboards;

    private ArrayList<Player> passwordPlayers;

    @Override
    public void onEnable() {
        LOGGER.info("[Football] Enabling FootballPlugin...");

        INSTANCE = this;

        // Plugin data directory located in /plugins/footballplugin
        this.dataDirectory = new File("plugins/footballplugin/");
        boolean directoryCreated = dataDirectory.mkdirs();
        if(directoryCreated)
            LOGGER.info("[Football] Plugin data directory created");

        this.worldsDirectory = new File(dataDirectory, "worlds/");
        worldsDirectory.mkdirs();

        // Config loading
        ConfigLoader.loadConfig(new File(dataDirectory, "config.json"));

        // LanguageManager initialization
        try {
            this.languageManager = new LanguageManager(new File(dataDirectory, "language.json"));
            FootTeamQualifier.initLanguage(languageManager);
            FootMode.initLanguage(languageManager);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Unable to initialize LanguageManager");
            exception.printStackTrace();
        }

        // BallManager initialization
        this.ballManager = new BallManager();

        // ArenaManager initialization
        this.arenaManager = new ArenaManager();

        // PlayerUtils static initialization
        this.frozePlayers = new HashMap<>();
        this.scoreboards = new HashMap<>();

        // Services initialization
        this.worldService = new MinecraftWorldServiceImpl(worldsDirectory, new File(dataDirectory, "maps.json"));
        worldService.init(this);

        this.playerService = new FootPlayerServiceImpl();
        playerService.init(this);

        this.inventoryService = new InventoryServiceImpl();
        inventoryService.init(this);

        this.matchmakingService = new MatchmakingServiceImpl();
        matchmakingService.init(this);

        this.rankingService = new RankingServiceImpl();
        rankingService.init(this);

        this.passwordPlayers = new ArrayList<>();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this, inventoryService), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BallListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        // Create commands
        Bukkit.getPluginCommand("football").setExecutor(new FootballCommand(this));

        LOGGER.fine("[Football] Plugin enabled!");
    }

    @Override
    public void onDisable() {
        LOGGER.info("[Football] Disabling Footballplugin...");

        this.rankingService.exit();
        this.matchmakingService.exit();
        this.inventoryService.exit();
        this.playerService.exit();

        ballManager.exit();
        arenaManager.exit();

        this.worldService.exit();

        LOGGER.fine("[Football] Plugin disabled!");
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    public File getWorldsDirectory() {
        return worldsDirectory;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public MinecraftWorldService getWorldService() {
        return worldService;
    }

    public BallManager getBallManager() {
        return ballManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public HashMap<OfflinePlayer, SerializableLocation> getFrozePlayers() {
        return frozePlayers;
    }

    public HashMap<Player, ScoreboardSign> getScoreboards() {
        return scoreboards;
    }

    public MatchmakingService getMatchmakingService() {
        return matchmakingService;
    }

    public FootPlayerService getPlayerService() {
        return playerService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public RankingService getRankingService() {
        return rankingService;
    }

}
