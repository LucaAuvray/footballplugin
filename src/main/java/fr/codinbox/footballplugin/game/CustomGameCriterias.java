package fr.codinbox.footballplugin.game;

import fr.codinbox.footballplugin.map.FootMap;

public class CustomGameCriterias {

    private FootMap map;
    private boolean allowSpectators;
    private boolean allowPlayerDamages;
    private String password;

    public CustomGameCriterias(FootMap map, boolean allowSpectators, boolean allowPlayerDamages, String password) {
        this.map = map;
        this.allowSpectators = allowSpectators;
        this.allowPlayerDamages = allowPlayerDamages;
        this.password = password;
    }

    public FootMap getMap() {
        return map;
    }

    public boolean isAllowSpectators() {
        return allowSpectators;
    }

    public boolean isAllowPlayerDamages() {
        return allowPlayerDamages;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return !password.isEmpty();
    }

    public CustomGameCriterias setMap(FootMap map) {
        this.map = map;
        return this;
    }

    public CustomGameCriterias setAllowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
        return this;
    }

    public CustomGameCriterias setAllowPlayerDamages(boolean allowPlayerDamages) {
        this.allowPlayerDamages = allowPlayerDamages;
        return this;
    }

    public CustomGameCriterias setPassword(String password) {
        this.password = password;
        return this;
    }

}
