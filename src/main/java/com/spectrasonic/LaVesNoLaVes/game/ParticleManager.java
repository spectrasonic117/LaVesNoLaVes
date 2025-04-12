package com.spectrasonic.LaVesNoLaVes.game;

import com.spectrasonic.LaVesNoLaVes.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ParticleManager {

    private final Main plugin;
    private final BridgeGame bridgeGame;
    private final Map<String, List<ParticlePoint>> particleSchemes;
    private final List<Location> particlePoints;
    private int particleCount;

    public ParticleManager(Main plugin, BridgeGame bridgeGame) {
        this.plugin = plugin;
        this.bridgeGame = bridgeGame;
        this.particleSchemes = new HashMap<>();
        this.particlePoints = new ArrayList<>();

        loadConfig();
        initParticleSchemes();
    }

    private void loadConfig() {
        ConfigurationSection particlePointsSection = plugin.getConfig().getConfigurationSection("particle_points");
        if (particlePointsSection != null) {
            particlePoints.clear();
            int index = 1;
        for (String key : particlePointsSection.getKeys(false)) {
            ConfigurationSection pointSection = particlePointsSection.getConfigurationSection(key);
                if (pointSection != null) {
            double x = pointSection.getDouble("x");
            double y = pointSection.getDouble("y");
            double z = pointSection.getDouble("z");
                particlePoints.add(new Location(Bukkit.getWorlds().get(0), x, y, z));
                } else {
                    Map<String, Object> pointMap = particlePointsSection.getConfigurationSection(key).getValues(false);
                    double x = (double) pointMap.get("x");
                    double y = (double) pointMap.get("y");
                    double z = (double) pointMap.get("z");
                    particlePoints.add(new Location(Bukkit.getWorlds().get(0), x, y, z));
        }
                    index++;
                }
        } else {
            plugin.getLogger().warning("No se encontró la sección 'particle_points' en el archivo de configuración");
        }
        particleCount = plugin.getConfig().getInt("particle_count", 500);
    }

    private void initParticleSchemes() {
        Map<String, Map<Integer, Integer>> schemes = new HashMap<>();
        Map<Integer, Integer> bridge1 = new HashMap<>();
        bridge1.put(1, 5);
        bridge1.put(4, 5);
        bridge1.put(9, 5);
        schemes.put("bridge_1", bridge1);

        Map<Integer, Integer> bridge2 = new HashMap<>();
        bridge2.put(1, 10);
        bridge2.put(2, 5);
        bridge2.put(4, 10);
        bridge2.put(5, 5);
        bridge2.put(7, 5);
        bridge2.put(8, 5);
        bridge2.put(9, 10);
        schemes.put("bridge_2", bridge2);

        Map<Integer, Integer> bridge3 = new HashMap<>();
        bridge3.put(1, 15);
        bridge3.put(2, 10);
        bridge3.put(3, 5);
        bridge3.put(4, 15);
        bridge3.put(5, 10);
        bridge3.put(6, 5);
        bridge3.put(7, 10);
        bridge3.put(8, 10);
        bridge3.put(9, 15);
        schemes.put("bridge_3", bridge3);

        for (Map.Entry<String, Map<Integer, Integer>> entry : schemes.entrySet()) {
            List<ParticlePoint> points = new ArrayList<>();
            for (Map.Entry<Integer, Integer> point : entry.getValue().entrySet()) {
                if (point.getKey() - 1 < particlePoints.size()) {
                    points.add(new ParticlePoint(particlePoints.get(point.getKey() - 1), point.getValue()));
                } else {
                    plugin.getLogger().warning("Índice de partícula fuera de rango para el schematic " + entry.getKey());
    }
            }
            particleSchemes.put(entry.getKey(), points);
        }
    }

    public void displayParticles(String schematicName) {
        List<ParticlePoint> points = particleSchemes.get(schematicName);
        if (points != null) {
            new BukkitRunnable() {
                int index = 0;

                @Override
                public void run() {
                    if (index >= points.size() || !bridgeGame.isRunning()) {
                        this.cancel();
                        return;
                    }
                    ParticlePoint point = points.get(index);
                    displayParticle(point.getLocation(), point.getRadius());
                    displayParticleRing(point.getLocation(), point.getRadius());
                    index++;
                }
            }.runTaskTimer(plugin, 0L, 10L);
        }
    }

    private void displayParticle(Location location, int radius) {
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, particleCount, radius / 2.0, 0, radius / 2.0, 0);
    }

    private void displayParticleRing(Location location, int radius) {
        int count = (int) (2 * Math.PI * radius);
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);
            Location particleLocation = new Location(location.getWorld(), x, location.getY(), z);
            particleLocation.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 1, 0, 0, 0, 0);
        }
    }

    private static class ParticlePoint {
        private final Location location;
        private final int radius;

        public ParticlePoint(Location location, int radius) {
            this.location = location;
            this.radius = radius;
        }

        public Location getLocation() {
            return location;
        }

        public int getRadius() {
            return radius;
        }
    }
}
