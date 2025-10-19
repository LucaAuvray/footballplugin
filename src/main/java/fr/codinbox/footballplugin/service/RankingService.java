package fr.codinbox.footballplugin.service;

import fr.codinbox.footballplugin.mode.FootMode;
import fr.codinbox.footballplugin.player.ComparablePlayerList;
import fr.codinbox.footballplugin.ranking.Rank;

import java.util.ArrayList;

public interface RankingService extends PluginService {

    ArrayList<Rank> getRanks();

    Rank getRankByMmr(double mmr);

    double calculateMmr(boolean win, int goals, int assists, int gameTime);

    ComparablePlayerList getRankings(FootMode mode, boolean recalculate);
}
