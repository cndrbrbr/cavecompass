package de.cndrbrbr.cavecompass;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

/**
 * Periodically re-scans each online player's inventory for Cave Compass
 * items and, for anyone carrying one, re-points it at the nearest cave.
 */
public final class CompassUpdateTask extends BukkitRunnable {

    private final CaveCompass plugin;
    private final NamespacedKey markerKey;
    private final int searchRadius;
    private final boolean onlyLoadedChunks;
    private final int minOpenBlocks;
    private final int opennessCheckRadius;

    public CompassUpdateTask(
            CaveCompass plugin,
            NamespacedKey markerKey,
            int searchRadius,
            boolean onlyLoadedChunks,
            int minOpenBlocks,
            int opennessCheckRadius) {
        this.plugin = plugin;
        this.markerKey = markerKey;
        this.searchRadius = searchRadius;
        this.onlyLoadedChunks = onlyLoadedChunks;
        this.minOpenBlocks = minOpenBlocks;
        this.opennessCheckRadius = opennessCheckRadius;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Inventory inventory = player.getInventory();

            boolean hasCompass = false;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (CaveCompassItem.isCaveCompass(markerKey, inventory.getItem(i))) {
                    hasCompass = true;
                    break;
                }
            }
            if (!hasCompass) {
                continue;
            }

            Location origin = player.getLocation();
            BlockLookup lookup = new OpenCaveLookup(
                    new BukkitWorldLookup(origin.getWorld()),
                    opennessCheckRadius,
                    minOpenBlocks
            );

            Optional<CaveFinder.Result> result = CaveFinder.findNearestCave(
                    lookup,
                    origin.getBlockX(),
                    origin.getBlockY(),
                    origin.getBlockZ(),
                    searchRadius,
                    onlyLoadedChunks
            );

            if (result.isEmpty()) {
                continue;
            }

            CaveFinder.Result cave = result.get();
            Location target = new Location(origin.getWorld(), cave.x() + 0.5, cave.y(), cave.z() + 0.5);
            int deltaY = cave.y() - origin.getBlockY();

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (CaveCompassItem.isCaveCompass(markerKey, item)) {
                    CaveCompassItem.pointAt(item, target, deltaY);
                    inventory.setItem(i, item);
                }
            }
        }
    }
}
