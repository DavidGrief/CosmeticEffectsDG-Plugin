package ru.dg.cosmetics.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class CosmeticMenuHolder implements InventoryHolder {

    private final UUID owner;

    public CosmeticMenuHolder(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
