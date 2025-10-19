package fr.codinbox.footballplugin.language;

import java.util.HashMap;
import java.util.Locale;

public enum LanguageKey {

    COMMAND_UNAVAILABLE("§cCommand unavailable for the moment."),
    NOT_IN_ARENA("§cYou are  not in an arena."),
    COMMAND_NOT_ENOUGH_ARGUMENTS("§6Not enough arguments!"),
    BLUE_TEAM_NAME("BLUE"),
    RED_TEAM_NAME("RED"),
    NO_PERMISSION("§cYou don't have the permission to do this action."),
    CASUAL("Casual"),
    RANKED("Ranked"),
    CUSTOM("Custom"),
    TWO_VERSUS_TWO("2v2"),
    THREE_VERSUS_THREE("3v3"),
    MAP_NAME_ALREADY_EXIST("§6The map name '%name%' is already taken!"),
    MAP_NAME_NOT_EXIST("§6The map name '%name%' does not exist!"),
    MAP_LOADING("§bLoading map '%name%'..."),
    MAP_DELETED("§aMap '%name%' deleted!"),
    MAP_UNLOADING("§bUnloading map '%name%'..."),
    MAP_TELEPORTED("§dTeleported you into your new world!"),
    EDITION_MODE_ON("§aActivated edition mode."),
    EDITION_MODE_OFF("§cDisabled edition mode."),
    NO_EDITING_MAP("§cYou aren't editing a map!"),
    GUI_EDITOR_TITLE("Map %name%"),
    GUI_LOCATION_EDITOR_TITLE("Map %name% > Spawns > %team%"),
    SPAWNPOINT_SET("§7Spawnpoint set! (%current%/%max%)"),
    SPAWNPOINT_SET_NO_INFOS("§7Spawnpoint set!"),
    ARENA_PLAYER_JOIN("§a%name% §7joined the game §8(%current%/%max%)"),
    ARENA_PLAYER_JOIN_NO_COUNT("§a%name% §7joined the game"),
    ARENA_PLAYER_LEAVE("§6%name% §7left the game §8(%current%/%max%)"),
    ARENA_PLAYER_LEAVE_NO_COUNT("§6%name% §7left the game"),
    ARENA_PLAYER_SPECTATOR_JOIN("§8%name% is now spectating the match"),
    ACTIONBAR_WAITING_PLAYERS("§7Waiting for players | §a%current%§8/§6%max%"),
    ACTIONBAR_WAITING_PLAYERS_NO_COUNT("§7Waiting for players"),
    POOF("§bPoof!"),
    GAME_STARTING_COUNTDOWN("§7Game starting in §b%seconds% §7second(s)"),
    ROUND_STARTING_COUNTDOWN("§7Playing in §b%seconds% §7second(s)"),
    BOSS_TEAM_INFO("%teamcolor%YOU ARE IN THE %name% TEAM"),
    BOSS_GOALS("%currentteamcolor%%currentteamname% %currentteamcolor%§l%currentteamgoals% §8- %enemyteamcolor%§l%enemyteamgoals% %enemyteamcolor%%enemyteamname%"),
    GOAL_SCORED("%playerteamcolor%%playername% §7scored a goal for the %scoreteamcolor%%scoreteamname% §7team! %scoreteamcolor%%scoreteamname% %scoreteamcolor%§l%scoreteamgoals% §8- %scoredteamcolor%§l%scoredteamgoals% %scoredteamcolor%%scoredteamname%"),
    GOAL_ASSIST("§8%playername% assisted him."),
    GO("§dGO!"),
    GAME_MINUTE_ALERT("§6%time% §7minute(s) remaining!"),
    GAME_SECONDS_ALERT("§6%time% §7second(s) remaining!"),
    GAME_ENDING_MINUTES_ALERT("§cWarning! §7The game is ending in §c%time% §7minute(s)!"),
    GAME_ENDING_SECONDS_ALERT("§cWarning! §7The game is ending in §c%time% §7second(s)!"),
    GAME_OVERTIME_MESSAGE("§8+%minutes% minute(s) and %seconds% second(s) (overtime)"),
    QUEUE_JOIN("§7You §ajoined §7the §b%mode% %submode% §7queue!"),
    QUEUE_LEAVE("§7You §6left §7the waiting queue"),
    CUSTOM_ARENA_CREATING_MESSAGE("§aCreating your custom arena, please wait..."),
    TEAM_SELECT_MESSAGE("§7To choose a team, type §b/fb team"),
    GAME_ALREADY_STARTED_MESSAGE("§cThe game has already started!"),
    ARENA_FULL("§cThis arena is currently full"),
    PROMPT_PASSWORD_MESSAGE("§aPlease enter the custom game password:"),
    WRONG_PASSWORD_MESSAGE("§cWrong password."),
    ALREADY_IN_GAME_MESSAGE("§cYou are already ingame!"),
    CANT_SPECTATE_CUSTOM_GAME("§cYou can't spectate a custom game by the spectate command. You must use the /fb menu!"),
    CUSTOM_ADMIN_MESSAGE("§7You are the admin of this custom game, to start the game type §a/fb start§7. To end it type §6/fb stop"),
    CUSTOM_NO_SPECTATORS_MESSAGE("§cThis game doesn't allow spectators."),
    CUSTOM_NOT_OWNER("§cYou are not the owner of this game.");

    private final String defaultValue;

    LanguageKey(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return name().toUpperCase(Locale.ROOT);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static HashMap<String, String> mapDefaultValues() {
        final HashMap<String, String> defaultValues = new HashMap<>();
        for (LanguageKey value : values()) {
            defaultValues.put(value.getKey(), value.getDefaultValue());
        }
        return defaultValues;
    }

    public static LanguageKey toLanguageKey(String key) {
        for(LanguageKey languageKey : values())
            if(languageKey.getKey().equals(key))
                return languageKey;
        throw new IllegalArgumentException("Language key '" + key + "' not found");
    }

}
