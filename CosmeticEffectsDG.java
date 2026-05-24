package ru.dg.cosmetics.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import ru.dg.cosmetics.CosmeticEffectsDG;
import ru.dg.cosmetics.model.AnimationType;
import ru.dg.cosmetics.model.CosmeticEffect;
import ru.dg.cosmetics.model.PlayerCosmeticData;
import ru.dg.cosmetics.model.SoundConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ParticleEffectManager {

    private final CosmeticEffectsDG plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Integer> soundTicks = new HashMap<>();
    private BukkitTask task;
    private long tick;
    private int updateTicks = 2;

    public ParticleEffectManager(CosmeticEffectsDG plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        updateTicks = Math.max(1, plugin.getConfig().getInt("settings.update-ticks", 2));
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::run, updateTicks, updateTicks);
    }

    public void restart() {
        lastLocations.clear();
        soundTicks.clear();
        tick = 0;
        start();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void run() {
        tick++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerCosmeticData data = plugin.getPlayerDataManager().getOrCreate(player);
            String active = data.getActive();
            if (active == null || active.isBlank()) continue;

            CosmeticEffect effect = plugin.getCosmeticManager().getEffect(active);
            if (effect == null || !effect.isEnabled()) continue;
            if (!data.hasEffect(effect.getId()) && !player.hasPermission("cosmetics.bypass")) continue;

            boolean moving = isMoving(player);
            if (effect.getAnimationType() == AnimationType.TRAIL
                    && plugin.getConfig().getBoolean("settings.only-when-moving-for-trails", true)
                    && !moving) {
                continue;
            }

            spawnEffect(player, effect);
            playSoundIfNeeded(player, effect);
        }
    }

    private boolean isMoving(Player player) {
        Location now = player.getLocation();
        Location last = lastLocations.put(player.getUniqueId(), now.clone());
        if (last == null || last.getWorld() == null || now.getWorld() == null) return true;
        if (!last.getWorld().equals(now.getWorld())) return true;
        return last.distanceSquared(now) > 0.0064;
    }

    private void spawnEffect(Player player, CosmeticEffect effect) {
        switch (effect.getAnimationType()) {
            case TRAIL -> spawnTrail(player, effect);
            case SPIRAL -> spawnSpiral(player, effect);
            case RING -> spawnRing(player, effect);
            case PULSE -> spawnPulse(player, effect);
            case AURA -> spawnAura(player, effect);
        }
    }

    private void spawnTrail(Player player, CosmeticEffect effect) {
        Location base = player.getLocation().clone().subtract(player.getLocation().getDirection().normalize().multiply(0.45));
        base.add(0, effect.getHeight(), 0);
        spawnToViewers(player, effect.getParticle(), base, effect.getAmount(), effect.getRadius(), 0.05, effect.getRadius(), effect.getSpeed());
        if (effect.getSecondaryParticle() != null) {
            spawnToViewers(player, effect.getSecondaryParticle(), base, Math.max(1, effect.getAmount() / 2), effect.getRadius(), 0.03, effect.getRadius(), effect.getSpeed());
        }
    }

    private void spawnAura(Player player, CosmeticEffect effect) {
        Location center = player.getLocation().clone().add(0, effect.getHeight(), 0);
        int points = Math.max(6, effect.getAmount());
        double angleBase = tick * effect.getSpeed() * 8.0;
        for (int i = 0; i < points; i++) {
            double angle = angleBase + (Math.PI * 2 * i / points);
            Location loc = center.clone().add(Math.cos(angle) * effect.getRadius(), Math.sin(angle * 1.5) * 0.25, Math.sin(angle) * effect.getRadius());
            spawnToViewers(player, effect.getParticle(), loc, 1, 0, 0, 0, effect.getSpeed());
        }
    }

    private void spawnSpiral(Player player, CosmeticEffect effect) {
        Location base = player.getLocation().clone();
        int points = Math.max(8, effect.getAmount());
        for (int i = 0; i < points; i++) {
            double progress = (double) i / points;
            double y = progress * effect.getHeight();
            double angle = tick * effect.getSpeed() * 10 + progress * Math.PI * 4;
            Location loc = base.clone().add(Math.cos(angle) * effect.getRadius(), y + 0.1, Math.sin(angle) * effect.getRadius());
            spawnToViewers(player, effect.getParticle(), loc, 1, 0, 0, 0, effect.getSpeed());
        }
    }

    private void spawnRing(Player player, CosmeticEffect effect) {
        Location center = player.getLocation().clone().add(0, effect.getHeight(), 0);
        int points = Math.max(8, effect.getAmount());
        double rotation = tick * effect.getSpeed() * 6.0;
        for (int i = 0; i < points; i++) {
            double angle = rotation + Math.PI * 2 * i / points;
            Location loc = center.clone().add(Math.cos(angle) * effect.getRadius(), 0, Math.sin(angle) * effect.getRadius());
            spawnToViewers(player, effect.getParticle(), loc, 1, 0, 0, 0, effect.getSpeed());
        }
    }

    private void spawnPulse(Player player, CosmeticEffect effect) {
        double pulse = 0.45 + 0.55 * Math.abs(Math.sin(tick * effect.getSpeed() * 5.0));
        double radius = Math.max(0.1, effect.getRadius() * pulse);
        Location center = player.getLocation().clone().add(0, effect.getHeight(), 0);
        spawnToViewers(player, effect.getParticle(), center, effect.getAmount(), radius, 0.15, radius, effect.getSpeed());
    }

    private void spawnToViewers(Player owner, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        World world = location.getWorld();
        if (world == null || particle == null) return;

        double distance = plugin.getConfig().getDouble("settings.particle-view-distance", 32.0);
        double distanceSq = distance * distance;
        for (Player viewer : world.getPlayers()) {
            if (viewer.getLocation().distanceSquared(location) <= distanceSq) {
                viewer.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
            }
        }
    }

    private void playSoundIfNeeded(Player player, CosmeticEffect effect) {
        SoundConfig sound = effect.getSoundConfig();
        if (sound == null || !sound.isEnabled()) return;
        int current = soundTicks.getOrDefault(player.getUniqueId(), 0) + updateTicks;
        if (current >= sound.getIntervalTicks()) {
            player.getWorld().playSound(player.getLocation(), sound.getSound(), sound.getVolume(), sound.getPitch());
            current = 0;
        }
        soundTicks.put(player.getUniqueId(), current);
    }
}
