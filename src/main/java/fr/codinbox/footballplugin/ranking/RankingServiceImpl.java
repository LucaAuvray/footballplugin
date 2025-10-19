package fr.codinbox.footballplugin.ranking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.codinbox.footballplugin.FootballPlugin;
import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.ComparablePlayer;
import fr.codinbox.footballplugin.player.ComparablePlayerList;
import fr.codinbox.footballplugin.service.RankingService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Logger;

public class RankingServiceImpl implements RankingService {

    private static final Logger LOGGER = Bukkit.getLogger();

    private File dataDirectory;
    private File configFile;

    private ArrayList<Rank> ranks;

    @Override
    public void init(Plugin plugin) {
        FootballPlugin footballPlugin = (FootballPlugin) plugin;

        dataDirectory = footballPlugin.getDataDirectory();
        configFile = new File(dataDirectory, "ranks.json");

        this.ranks = new ArrayList<>();

        if(!configFile.exists())
            createConfig();
        else
            loadConfig();

        if(ranks.isEmpty()) {
            LOGGER.info("[Football] Created basic ranks");
            ranks.add(new Rank("Example", "§a", 0d, Double.MAX_VALUE, 1));
            saveConfig();
        }

        for(Rank rank : ranks)
            LOGGER.info("[Football] Loaded rank " + rank.getName() + " (" + rank.getMinMmr() + "-" + rank.getMaxMmr() + ")");
    }

    private void createConfig() {
        ranks.add(new Rank("Example", "§a", 0d, Double.MAX_VALUE, 1));

        saveConfig();
        loadConfig();
    }

    private RankCapacitor toCapacitor() {
        ArrayList<SerializableRank> serializableRanks = new ArrayList<>();
        this.ranks.forEach(rank -> serializableRanks.add(rank.serialize()));
        return new RankCapacitor(serializableRanks);
    }

    private void loadConfig() {
        final Gson gson = new Gson();
        try {
            RankCapacitor capacitor = gson.fromJson(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8), RankCapacitor.class);
            for(SerializableRank serializableRank : capacitor.ranks)
                this.ranks.add(serializableRank.toLegacyData());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try {
            final Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

            Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8);
            writer.write(gson.toJson(toCapacitor()));
            writer.flush();
            writer.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void exit() {
        saveConfig();
    }

    @Override
    public ArrayList<Rank> getRanks() {
        return this.ranks;
    }

    @Override
    public Rank getRankByMmr(double mmr) {
        Rank lowestRank = getLowestRank();
        if(lowestRank.getMinMmr() > mmr)
            return lowestRank;

        Rank highestRank = getHighestRank();
        if(highestRank.getMaxMmr() < mmr)
            return highestRank;

        for(Rank rank : ranks)
            if(rank.isInRange(mmr))
                return rank;

        return null;
    }

    @Override
    public double calculateMmr(boolean win, int goals, int assists, int gameTime) {
        double total = FootConfig.RANKING_MMR_BASIC_VALUE;

        if(!win)
            return -total;

        goals = Math.min(goals, FootConfig.RANKING_MMR_MAX_GOALS);
        total += goals * FootConfig.RANKING_MMR_PER_GOAL;

        assists = Math.min(assists, FootConfig.RANKING_MMR_MAX_ASSISTS);
        total += assists * FootConfig.RANKING_MMR_PER_ASSIST;

        total += ((double) gameTime/1000)*Math.exp((double) gameTime/1000);

        return total;
    }

    @Override
    public ComparablePlayerList getRankings(FootMode mode, boolean recalculate) {
        return FootballPlugin.INSTANCE.getPlayerService().getLeaderboard(mode, recalculate);
    }

    private Rank getHighestRank() {
        return ranks.get(ranks.size() - 1);
    }

    private Rank getLowestRank() {
        return ranks.get(0);
    }

}
