package fr.codinbox.footballplugin.service;

import org.bukkit.plugin.Plugin;

public interface PluginService {

    void init(Plugin plugin);
    void exit();

}
