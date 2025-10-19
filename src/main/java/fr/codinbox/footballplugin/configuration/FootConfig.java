package fr.codinbox.footballplugin.configuration;

public class FootConfig {

    public static Float BALL_ADDITIONAL_PITCH = 10f;
    public static Float BALL_MAX_PITCH = -90f;
    public static Double BALL_HIT_POWER = 1.56d;
    public static String BALL_PLAYER_NAME = "dandan2611";
    public static Double BALL_REACH_DISTANCE = 1.25d;
    public static Integer BALL_PHYSICS_TICK = 1;
    public static Long MAX_ASSIST_TIME = 5000L;

    public static Integer GAME_START_TIMER = 15;
    public static Integer GAME_GAME_TIMER = 5*60;
    public static Integer GAME_OVERTIME_TIMER = 60;
    public static Integer GAME_MAX_SPECTATORS = 5;

    public static Integer MATCHMAKING_LOOP_SECONDS = 5;
    public static Integer MATCHMAKING_MIN_LOOPS = 2;
    public static Integer MATCHMAKING_MAX_ARENAS = 10;
    public static Double MATCHMAKING_MAX_MMR_GAP = 100d;
    public static Integer MATCHMAKING_LOOP_MMR_GAP_UPDATE = 15;
    public static Double MATCHMAKING_LOOP_MMR_GAP_UPDATE_INCREMENT = 50d;

    public static Double RANKING_MMR_STARTING_MMR = 500d;
    public static Double RANKING_MMR_BASIC_VALUE = 8d;
    public static Integer RANKING_MMR_MAX_GOALS = 3;
    public static Integer RANKING_MMR_MAX_ASSISTS = 2;
    public static Double RANKING_MMR_PER_GOAL = 0.15d;
    public static Double RANKING_MMR_PER_ASSIST = 0.1d;

    public static Integer CURRENT_SEASON = 0;

}
