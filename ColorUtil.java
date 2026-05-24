package ru.dg.cosmetics.manager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.dg.cosmetics.CosmeticEffectsDG;
import ru.dg.cosmetics.config.ConfigManager;
import ru.dg.cosmetics.model.PlayerCosmeticData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerDataManager {

    private final CosmeticEffectsDG plugin;
    private final ConfigManager configManager;
    private final Map<UUID, PlayerCosmeticData> cache = new LinkedHashMap<>();

    public PlayerDataManager(CosmeticEffectsDG plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        cache.clear();
        ConfigurationSection root = configManager.players().getConfigurationSection("players");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection section = root.getConfigurationSection(key);
                if (section == null) continue;
                String name = section.getString("name", "Unknown");
                Set<String> unlocked = new LinkedHashSet<>();
                for (String id : section.getStringList("unlocked")) {
                    unlocked.add(id.toLowerCase(Locale.ROOT));
                }
                String active = section.getString("active", null);
                if (active != null) active = active.toLowerCase(Locale.ROOT);
                cache.put(uuid, new PlayerCosmeticData(uuid, name, unlocked, active));
            } catch (Exception ex) {
                plugin.getLogger().warning("Cannot load player cosmetics data: " + key);
            }
        }
    }

    public void save() {
        configManager.players().set("players", null);
        for (PlayerCosmeticData data : cache.values()) {
            String path = "players." + data.getUuid();
            configManager.players().set(path + ".name", data.getName());
            configManager.players().set(path + ".unlocked", new ArrayList<>(data.getUnlocked()));
            configManager.players().set(path + ".active", data.getActive());
        }
        configManager.savePlayers();
    }

    public PlayerCosmeticData getOrCreate(Player player) {
        PlayerCosmeticData data = cache.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerCosmeticData(uuid, player.getName()));
        data.setName(player.getName());
        return data;
    }

    public PlayerCosmeticData getOrCreate(OfflinePlayer player, String fallbackName) {
        UUID uuid = player.getUniqueId();
        String name = player.getName() == null ? fallbackName : player.getName();
        PlayerCosmeticData data = cache.computeIfAbsent(uuid, id -> new PlayerCosmeticData(id, name));
        data.setName(name);
        return data;
    }

    public PlayerCosmeticData get(Player player) {
        return cache.get(player.getUniqueId());
    }

    public PlayerCosmeticData get(UUID uuid) {
        return cache.get(uuid);
    }

    @SuppressWarnings("deprecation")
    public OfflinePlayer resolveOfflinePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        return Bukkit.getOfflinePlayer(name);
    }

    public void setActive(Player player, String effectId) {
        PlayerCosmeticData data = getOrCreate(player);
        data.setActive(effectId == null ? null : effectId.toLowerCase(Locale.ROOT));
        save();
    }

    public void disable(Player player) {
        setActive(player, null);
    }
}
