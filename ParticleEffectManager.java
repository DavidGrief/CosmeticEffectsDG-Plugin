package ru.dg.cosmetics.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.dg.cosmetics.CosmeticEffectsDG;

import java.io.File;
import java.io.IOException;

public final class ConfigManager {

    private final CosmeticEffectsDG plugin;
    private File cosmeticsFile;
    private File messagesFile;
    private File playersFile;
    private FileConfiguration cosmetics;
    private FileConfiguration messages;
    private FileConfiguration players;

    public ConfigManager(CosmeticEffectsDG plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        cosmeticsFile = new File(plugin.getDataFolder(), "cosmetics.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        cosmetics = YamlConfiguration.loadConfiguration(cosmeticsFile);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        players = YamlConfiguration.loadConfiguration(playersFile);
    }

    public FileConfiguration config() { return plugin.getConfig(); }
    public FileConfiguration cosmetics() { return cosmetics; }
    public FileConfiguration messages() { return messages; }
    public FileConfiguration players() { return players; }

    public void savePlayers() {
        try {
            players.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Cannot save players.yml: " + e.getMessage());
        }
    }
}
