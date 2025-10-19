package fr.codinbox.footballplugin.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.language.Language;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.service.FootPlayerService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class FootPlayerServiceImpl implements FootPlayerService {

    private final static Logger LOGGER = Bukkit.getLogger();

    private File dataDirectory;
    private HashMap<UUID, FootPlayer> data;
    private HashMap<FootMode, ComparablePlayerList> rankings;
    private ArrayList<UUID> toSave;

    private Thread saveThread;
    private int saveTaskId;

    @Override
    public void init(Plugin plugin) {
        this.data = new HashMap<>();
        this.rankings = new HashMap<>();
        this.toSave = new ArrayList<>();
        dataDirectory = new File(((FootballPlugin) plugin).getDataDirectory(), "players");
        if(!dataDirectory.exists())
            dataDirectory.mkdirs();

        this.saveThread = new Thread(() -> {
            saveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> this.saveAllDatas(false), 10*60*20, 10*60*20);
        });
        saveThread.start();
    }

    @Override
    public void exit() {
        if(saveThread != null && saveThread.isAlive() && !saveThread.isInterrupted()) {
            Bukkit.getScheduler().cancelTask(saveTaskId);
            saveThread.interrupt();
        }

        Bukkit.getLogger().info("[Football] Saving all player datas...");
        saveAllDatas(true);
        Bukkit.getLogger().info("[Football] Saved!");
    }

    @Override
    public FootPlayer createPlayerData(Player newPlayer) {
        return null;
    }

    @Override
    public FootPlayer getPlayerData(UUID player) {
        if(this.data.containsKey(player)) {
            return this.data.get(player);
        }
        else {
            // Load player data from file
            File dataFile = new File(dataDirectory, player + ".json");

            FootPlayer fp;
            if(dataFile.exists()) {
                final Gson gson = new Gson();
                try {
                    SerializableFootPlayer footPlayer = gson.fromJson(new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8), SerializableFootPlayer.class);
                    if(footPlayer == null)
                        return null;
                    fp = footPlayer.toLegacyFootPlayer();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else {
                fp = FootPlayer.newPlayer(Bukkit.getOfflinePlayer(player));
                saveData(fp);
                LOGGER.info("[Football] Created player datas of " + player.toString());
            }

            this.data.put(player, fp);
            return fp;
        }
    }

    @Override
    public FootPlayer getPlayerData(OfflinePlayer player) {
        return getPlayerData(player.getUniqueId());
    }

    @Override
    public ComparablePlayerList getLeaderboard(FootMode mode, boolean recalculate) {
        if(!recalculate && this.rankings.containsKey(mode))
            return this.rankings.get(mode);

        ComparablePlayerList players = new ComparablePlayerList();

        for(File file : dataDirectory.listFiles()) {
            String name = FilenameUtils.getBaseName(file.getName());
            UUID uuid = UUID.fromString(name);

            players.add(new ComparablePlayer(getPlayerData(uuid), mode));
        }

        Collections.sort(players);

        this.rankings.put(mode, players);

        return players;
    }

    @Override
    public boolean hasData(Player player) {
        return false;
    }

    @Override
    public void saveData(FootPlayer player) {
        try {
            final Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(dataDirectory, player.getOfflinePlayer().getUniqueId().toString() + ".json")), StandardCharsets.UTF_8);
            writer.write(gson.toJson(player.toSerializablePlayer()));
            writer.flush();
            writer.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void saveAllDatas(boolean force) {
        ArrayList<UUID> toUnsave = new ArrayList<>();

        for(UUID uuid : (force ? data.keySet() : toSave)) {
            if(data.get(uuid) != null) {
                saveData(data.get(uuid));
                toUnsave.add(uuid);
            }
        }

        toUnsave.forEach(uuid -> toSave.remove(uuid));
        LOGGER.info("[Football] Saved " + data.size() + " player datas");
    }

    @Override
    public void markAsSaving(Player player) {
        toSave.add(player.getUniqueId());
    }

}
