package de.cndrbrbr.cavecompass;

import org.bukkit.plugin.java.JavaPlugin;

public final class CaveCompass extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        long interval = getConfig().getLong("update-interval-ticks", 40L);
        int searchRadius = getConfig().getInt("search-radius", 48);
        int maxSearchRadius = getConfig().getInt("max-search-radius", 128);
        boolean onlyLoadedChunks = getConfig().getBoolean("only-loaded-chunks", true);
        int minOpenBlocks = getConfig().getInt("min-open-blocks", 20);
        int opennessCheckRadius = getConfig().getInt("openness-check-radius", 2);
        int defaultClusterSize = getConfig().getInt("default-cluster-size", 3);
        int clusterCheckRadius = getConfig().getInt("cluster-check-radius", 8);

        CaveCompassItem compassItem = new CaveCompassItem(this);
        CompassSettings defaults = new CompassSettings(CompassSettings.CAVE, searchRadius, 1);

        var giveCommand = getCommand("cavecompass");
        if (giveCommand != null) {
            GiveCompassCommand executor = new GiveCompassCommand(compassItem, defaults, maxSearchRadius, defaultClusterSize);
            giveCommand.setExecutor(executor);
            giveCommand.setTabCompleter(executor);
        }

        new CompassUpdateTask(this, compassItem, defaults, onlyLoadedChunks, minOpenBlocks, opennessCheckRadius, clusterCheckRadius)
                .runTaskTimer(this, interval, interval);
    }
}
