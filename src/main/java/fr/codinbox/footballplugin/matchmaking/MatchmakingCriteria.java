package fr.codinbox.footballplugin.matchmaking;

import fr.codinbox.footballplugin.configuration.FootConfig;
import fr.codinbox.footballplugin.map.FootMap;
import fr.codinbox.footballplugin.mode.FootMode;

public class MatchmakingCriteria {

    private FootMode mode = null;
    private FootMap map = null;
    private double maxMmrGap = FootConfig.MATCHMAKING_MAX_MMR_GAP;
    private boolean canPlayAgainstPreviousOpponents = true;
    private int minimumLoopTime = FootConfig.MATCHMAKING_MIN_LOOPS;
    private int currentLoopTime;

    public MatchmakingCriteria(FootMode mode, FootMap map, int maxMmrGap, boolean canPlayAgainstPreviousOpponents, int minimumLoopTime) {
        this.mode = mode;
        this.map = map;
        this.maxMmrGap = maxMmrGap;
        this.canPlayAgainstPreviousOpponents = canPlayAgainstPreviousOpponents;
        this.minimumLoopTime = minimumLoopTime;
        this.currentLoopTime = 0;
    }

    public MatchmakingCriteria(FootMode mode, FootMap map) {
        this.mode = mode;
        this.map = map;
    }

    public MatchmakingCriteria setMode(FootMode mode) {
        this.mode = mode;
        return this;
    }

    public MatchmakingCriteria setMap(FootMap map) {
        this.map = map;
        return this;
    }

    public MatchmakingCriteria setMaxMmrGap(double maxMmrGap) {
        this.maxMmrGap = maxMmrGap;
        return this;
    }

    public MatchmakingCriteria setCanPlayAgainstPreviousOpponents(boolean canPlayAgainstPreviousOpponents) {
        this.canPlayAgainstPreviousOpponents = canPlayAgainstPreviousOpponents;
        return this;
    }

    public MatchmakingCriteria setMinimumLoopTime(int minimumLoopTime) {
        this.minimumLoopTime = minimumLoopTime;
        return this;
    }

    public FootMode getMode() {
        return mode;
    }

    public FootMap getMap() {
        return map;
    }

    public double getMaxMmrGap() {
        return maxMmrGap;
    }

    public boolean isCanPlayAgainstPreviousOpponents() {
        return canPlayAgainstPreviousOpponents;
    }

    public int getMinimumLoopTime() {
        return minimumLoopTime;
    }

    public int getCurrentLoopTime() {
        return currentLoopTime;
    }

    protected void loop() {
        this.currentLoopTime++;
    }

}
