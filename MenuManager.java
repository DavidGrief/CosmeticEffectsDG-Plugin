package ru.dg.cosmetics.manager;

import org.bukkit.command.CommandSender;

public final class PermissionManager {

    public boolean has(CommandSender sender, String permission) {
        return sender.hasPermission("cosmetics.admin") || sender.hasPermission(permission);
    }
}
