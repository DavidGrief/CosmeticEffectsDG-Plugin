package ru.dg.cosmetics.manager;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class StorageManager {

    private final JavaPlugin plugin;

    public StorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createDefaultFiles() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        saveIfMissing("config.yml");
        saveIfMissing("cosmetics.yml");
        saveIfMissing("messages.yml");
        saveIfMissing("players.yml");
    }

    private void saveIfMissing(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) plugin.saveResource(name, false);
    }
}
