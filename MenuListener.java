package ru.dg.cosmetics.manager;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import ru.dg.cosmetics.CosmeticEffectsDG;
import ru.dg.cosmetics.config.ConfigManager;
import ru.dg.cosmetics.model.AnimationType;
import ru.dg.cosmetics.model.CosmeticEffect;
import ru.dg.cosmetics.model.SoundConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class CosmeticManager {

    private final CosmeticEffectsDG plugin;
    private final ConfigManager configManager;
    private final Map<String, CosmeticEffect> effects = new LinkedHashMap<>();

    public CosmeticManager(CosmeticEffectsDG plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void load() {
        effects.clear();
        ConfigurationSection root = configManager.cosmetics().getConfigurationSection("cosmetics");
        if (root == null) {
            plugin.getLogger().warning("cosmetics.yml does not contain cosmetics section.");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;
            CosmeticEffect effect = parseEffect(id.toLowerCase(Locale.ROOT), section);
            if (effect != null) effects.put(effect.getId(), effect);
        }
    }

    private CosmeticEffect parseEffect(String id, ConfigurationSection section) {
        Material material = parseMaterial(section.getString("menu-item", "BARRIER"), Material.BARRIER);
        Particle particle = parseParticle(section.getString("particle", "CLOUD"));
        if (particle == null) {
            plugin.getLogger().warning("Invalid particle for cosmetic " + id + ". Effect skipped.");
            return null;
        }
        Particle secondary = parseParticle(section.getString("secondary-particle", null));
        SoundConfig sound = parseSound(section.getConfigurationSection("sound"));

        return new CosmeticEffect(
                id,
                section.getBoolean("enabled", true),
                section.getString("display-name", id),
                material,
                section.getInt("slot", 0),
                AnimationType.fromString(section.getString("animation-type", "AURA")),
                particle,
                secondary,
                Math.max(1, section.getInt("amount", 8)),
                Math.max(0.05, section.getDouble("radius", 1.0)),
                section.getDouble("speed", 0.02),
                Math.max(0.0, section.getDouble("height", 1.0)),
                sound,
                section.getString("permission", "cosmetics.effect." + id),
                section.getStringList("description")
        );
    }

    private SoundConfig parseSound(ConfigurationSection section) {
        if (section == null) return new SoundConfig(false, null, 0.2f, 1.0f, 100);
        Sound sound = null;
        String raw = section.getString("type", "");
        if (raw != null && !raw.isBlank()) {
            try {
                sound = Sound.valueOf(raw.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Invalid sound: " + raw);
            }
        }
        return new SoundConfig(
                section.getBoolean("enabled", false),
                sound,
                (float) section.getDouble("volume", 0.2),
                (float) section.getDouble("pitch", 1.0),
                section.getInt("interval-ticks", 100)
        );
    }

    private Material parseMaterial(String value, Material fallback) {
        if (value == null) return fallback;
        Material material = Material.matchMaterial(value);
        return material == null ? fallback : material;
    }

    private Particle parseParticle(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Particle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid particle: " + value);
            return null;
        }
    }

    public CosmeticEffect getEffect(String id) {
        if (id == null) return null;
        return effects.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String id) {
        return getEffect(id) != null;
    }

    public Collection<CosmeticEffect> getEffects() {
        return Collections.unmodifiableCollection(effects.values());
    }

    public Map<String, CosmeticEffect> getEffectsMap() {
        return Collections.unmodifiableMap(effects);
    }
}
