package de.cndrbrbr.cavecompass;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Periodically re-scans each online player's inventory for Cave Compass
 * items and re-points each one at the nearest match for its own settings.
 * Compasses with identical settings share a single search.
 */
public final class CompassUpdateTask extends BukkitRunnable {

    private final CaveCompass plugin;
    private final CaveCompassItem compassItem;
    private final CompassSettings defaults;
    private final boolean onlyLoadedChunks;
    private final int minOpenBlocks;
    private final int opennessCheckRadius;
    private final int clusterCheckRadius;

    public CompassUpdateTask(
            CaveCompass plugin,
            CaveCompassItem compassItem,
            CompassSettings defaults,
            boolean onlyLoadedChunks,
            int minOpenBlocks,
            int opennessCheckRadius,
            int clusterCheckRadius) {
        this.plugin = plugin;
        this.compassItem = compassItem;
        this.defaults = defaults;
        this.onlyLoadedChunks = onlyLoadedChunks;
        this.minOpenBlocks = minOpenBlocks;
        this.opennessCheckRadius = opennessCheckRadius;
        this.clusterCheckRadius = clusterCheckRadius;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Inventory inventory = player.getInventory();
            Location origin = player.getLocation();
            BukkitWorldLookup world = null;
            Map<CompassSettings, Optional<CaveFinder.Result>> results = new HashMap<>();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (!compassItem.isCaveCompass(item)) {
                    continue;
                }

                if (world == null) {
                    world = new BukkitWorldLookup(origin.getWorld(), onlyLoadedChunks);
                }
                BukkitWorldLookup lookup = world;
                CompassSettings settings = compassItem.settingsOf(item, defaults);
                Optional<CaveFinder.Result> result =
                        results.computeIfAbsent(settings, s -> search(lookup, origin, s));

                if (result.isEmpty()) {
                    compassItem.markNotFound(item, settings);
                } else {
                    CaveFinder.Result hit = result.get();
                    Location target = new Location(origin.getWorld(), hit.x() + 0.5, hit.y(), hit.z() + 0.5);
                    compassItem.pointAt(item, settings, target, hit.y() - origin.getBlockY());
                }
                inventory.setItem(i, item);
            }
        }
    }

    private Optional<CaveFinder.Result> search(BukkitWorldLookup world, Location origin, CompassSettings settings) {
        BlockLookup bounds = world;
        BlockMatcher matcher;

        if (settings.isCave()) {
            OpenCaveLookup caves = new OpenCaveLookup(world, opennessCheckRadius, minOpenBlocks);
            bounds = caves;
            matcher = caves::isCaveAir;
        } else {
            Material material = Material.matchMaterial(settings.target());
            if (material == null) {
                return Optional.empty();
            }
            matcher = new ClusterMatcher(world.matching(material), world, clusterCheckRadius, settings.clusterSize());
        }

        return CaveFinder.findNearest(
                bounds,
                matcher,
                origin.getBlockX(),
                origin.getBlockY(),
                origin.getBlockZ(),
                settings.radius(),
                onlyLoadedChunks
        );
    }
}
