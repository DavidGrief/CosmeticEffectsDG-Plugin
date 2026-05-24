package ru.dg.cosmetics.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.dg.cosmetics.CosmeticEffectsDG;
import ru.dg.cosmetics.config.MessageManager;
import ru.dg.cosmetics.model.CosmeticEffect;
import ru.dg.cosmetics.model.PlayerCosmeticData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class CommandManager implements CommandExecutor, TabCompleter {

    private final CosmeticEffectsDG plugin;
    private final MessageManager msg;

    public CommandManager(CosmeticEffectsDG plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return openMenu(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        try {
            return switch (sub) {
                case "help" -> help(sender);
                case "off" -> off(sender);
                case "active" -> active(sender);
                case "give" -> give(sender, args);
                case "remove" -> remove(sender, args);
                case "clear" -> clear(sender, args);
                case "list" -> list(sender);
                case "reload" -> reload(sender);
                case "setactive" -> setActive(sender, args);
                case "info" -> info(sender, args);
                default -> openMenu(sender);
            };
        } catch (Exception ex) {
            sender.sendMessage(msg.prefix() + msg.color("&cОшибка: &f" + ex.getMessage()));
            plugin.getLogger().warning("Command error: " + ex.getMessage());
            ex.printStackTrace();
            return true;
        }
    }

    private boolean openMenu(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!has(sender, "cosmetics.use")) return true;
        plugin.getMenuManager().openMenu(player);
        return true;
    }

    private boolean help(CommandSender sender) {
        msg.send(sender, "help");
        return true;
    }

    private boolean off(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!has(sender, "cosmetics.use")) return true;
        plugin.getPlayerDataManager().disable(player);
        msg.send(player, "effect-disabled");
        return true;
    }

    private boolean active(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return true;
        if (!has(sender, "cosmetics.use")) return true;
        PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(player);
        if (data.getActive() == null) {
            msg.send(player, "active-none");
            return true;
        }
        CosmeticEffect effect = plugin.getCosmeticManager().getEffect(data.getActive());
        String name = effect == null ? data.getActive() : effect.getDisplayName();
        msg.send(player, "active-effect", "%effect%", name);
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!has(sender, "cosmetics.give")) return true;
        if (args.length < 3) return usage(sender, "/cosmetics give <игрок> <эффект>");

        String playerName = args[1];
        String effectId = args[2].toLowerCase(Locale.ROOT);
        CosmeticEffect effect = plugin.getCosmeticManager().getEffect(effectId);
        if (effect == null) {
            msg.send(sender, "effect-not-found", "%effect%", effectId);
            return true;
        }

        OfflinePlayer offline = plugin.getPlayerDataManager().resolveOfflinePlayer(playerName);
        PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(offline, playerName);
        if (!data.addEffect(effect.getId())) {
            msg.send(sender, "effect-already-unlocked", "%player%", data.getName(), "%effect%", effect.getDisplayName());
            return true;
        }
        plugin.getPlayerDataManager().save();
        msg.send(sender, "effect-given", "%player%", data.getName(), "%effect%", effect.getDisplayName());
        Player online = Bukkit.getPlayer(offline.getUniqueId());
        if (online != null) msg.send(online, "effect-received", "%effect%", effect.getDisplayName());
        return true;
    }

    private boolean remove(CommandSender sender, String[] args) {
        if (!has(sender, "cosmetics.remove")) return true;
        if (args.length < 3) return usage(sender, "/cosmetics remove <игрок> <эффект>");

        String playerName = args[1];
        String effectId = args[2].toLowerCase(Locale.ROOT);
        CosmeticEffect effect = plugin.getCosmeticManager().getEffect(effectId);
        if (effect == null) {
            msg.send(sender, "effect-not-found", "%effect%", effectId);
            return true;
        }

        OfflinePlayer offline = plugin.getPlayerDataManager().resolveOfflinePlayer(playerName);
        PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(offline, playerName);
        if (!data.removeEffect(effect.getId())) {
            msg.send(sender, "effect-not-owned", "%player%", data.getName(), "%effect%", effect.getDisplayName());
            return true;
        }
        plugin.getPlayerDataManager().save();
        msg.send(sender, "effect-removed", "%player%", data.getName(), "%effect%", effect.getDisplayName());
        return true;
    }

    private boolean clear(CommandSender sender, String[] args) {
        if (!has(sender, "cosmetics.remove")) return true;
        if (args.length < 2) return usage(sender, "/cosmetics clear <игрок>");
        OfflinePlayer offline = plugin.getPlayerDataManager().resolveOfflinePlayer(args[1]);
        PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(offline, args[1]);
        data.clear();
        plugin.getPlayerDataManager().save();
        msg.send(sender, "effects-cleared", "%player%", data.getName());
        return true;
    }

    private boolean list(CommandSender sender) {
        if (!has(sender, "cosmetics.admin")) return true;
        msg.send(sender, "list-header");
        if (plugin.getCosmeticManager().getEffects().isEmpty()) {
            msg.send(sender, "empty-list");
            return true;
        }
        for (CosmeticEffect effect : plugin.getCosmeticManager().getEffects()) {
            sender.sendMessage(msg.color("&8- &f" + effect.getId() + " &7→ " + effect.getDisplayName() + " &8(" + effect.getAnimationType() + ")"));
        }
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!has(sender, "cosmetics.reload")) return true;
        plugin.reloadPlugin();
        msg.send(sender, "reload");
        return true;
    }

    private boolean setActive(CommandSender sender, String[] args) {
        if (!has(sender, "cosmetics.admin")) return true;
        if (args.length < 3) return usage(sender, "/cosmetics setactive <игрок> <эффект>");
        String playerName = args[1];
        String effectId = args[2].toLowerCase(Locale.ROOT);
        CosmeticEffect effect = plugin.getCosmeticManager().getEffect(effectId);
        if (effect == null) {
            msg.send(sender, "effect-not-found", "%effect%", effectId);
            return true;
        }

        OfflinePlayer offline = plugin.getPlayerDataManager().resolveOfflinePlayer(playerName);
        PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(offline, playerName);
        data.addEffect(effect.getId());
        data.setActive(effect.getId());
        plugin.getPlayerDataManager().save();
        msg.send(sender, "admin-set-active", "%player%", data.getName(), "%effect%", effect.getDisplayName());
        return true;
    }

    private boolean info(CommandSender sender, String[] args) {
        if (!has(sender, "cosmetics.admin")) return true;
        if (args.length < 2) return usage(sender, "/cosmetics info <игрок>");
        OfflinePlayer offline = plugin.getPlayerDataManager().resolveOfflinePlayer(args[1]);
        PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(offline, args[1]);
        msg.send(sender, "info-header", "%player%", data.getName());
        String unlocked = data.getUnlocked().isEmpty() ? msg.color("&7нет") : String.join(", ", data.getUnlocked());
        String active = data.getActive() == null ? msg.color("&7нет") : data.getActive();
        msg.send(sender, "info-unlocked", "%effects%", unlocked);
        msg.send(sender, "info-active", "%effect%", active);
        return true;
    }

    private boolean usage(CommandSender sender, String usage) {
        msg.send(sender, "invalid-usage", "%usage%", usage);
        return true;
    }

    private boolean has(CommandSender sender, String permission) {
        if (!plugin.getPermissionManager().has(sender, permission)) {
            msg.send(sender, "no-permission");
            return false;
        }
        return true;
    }

    private Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            msg.send(sender, "player-only");
            return null;
        }
        return player;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("off", "active", "help"));
            if (sender.hasPermission("cosmetics.admin") || sender.hasPermission("cosmetics.give")) subs.add("give");
            if (sender.hasPermission("cosmetics.admin") || sender.hasPermission("cosmetics.remove")) {
                subs.add("remove");
                subs.add("clear");
            }
            if (sender.hasPermission("cosmetics.admin")) {
                subs.add("list");
                subs.add("setactive");
                subs.add("info");
            }
            if (sender.hasPermission("cosmetics.admin") || sender.hasPermission("cosmetics.reload")) subs.add("reload");
            return filter(subs, args[0]);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && Arrays.asList("give", "remove", "clear", "setactive", "info").contains(sub)) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3 && Arrays.asList("give", "remove", "setactive").contains(sub)) {
            return filter(plugin.getCosmeticManager().getEffects().stream()
                    .map(CosmeticEffect::getId)
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList()), args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }
}
