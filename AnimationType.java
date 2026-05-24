package ru.dg.cosmetics.config;

import org.bukkit.command.CommandSender;
import ru.dg.cosmetics.CosmeticEffectsDG;
import ru.dg.cosmetics.util.ColorUtil;

import java.util.List;

public final class MessageManager {

    private final CosmeticEffectsDG plugin;
    private ConfigManager configManager;

    public MessageManager(CosmeticEffectsDG plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void reload() {
        this.configManager = plugin.getConfigurationManager();
    }

    public String prefix() {
        return color(configManager.messages().getString("prefix", "&#55ffff&lCosmeticsDG &8» &f"));
    }

    public String color(String text) {
        return ColorUtil.color(text == null ? "" : text);
    }

    public List<String> color(List<String> lines) {
        return ColorUtil.color(lines);
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        if (configManager.messages().isList(key)) {
            for (String line : configManager.messages().getStringList(key)) {
                sender.sendMessage(apply(line, placeholders));
            }
            return;
        }
        String raw = configManager.messages().getString(key, "&cMessage not found: " + key);
        sender.sendMessage(prefix() + apply(raw, placeholders));
    }

    public String apply(String raw, String... placeholders) {
        String result = raw == null ? "" : raw;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            result = result.replace(placeholders[i], placeholders[i + 1]);
        }
        return color(result);
    }
}
