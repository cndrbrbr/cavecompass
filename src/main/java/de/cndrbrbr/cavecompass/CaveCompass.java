package de.cndrbrbr.cavecompass;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class CaveCompass extends JavaPlugin {

    private NamespacedKey markerKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        markerKey = new NamespacedKey(this, "tracker");

        var giveCommand = getCommand("cavecompass");
        if (giveCommand != null) {
            giveCommand.setExecutor(new GiveCompassCommand(markerKey));
        }

        long interval = getConfig().getLong("update-interval-ticks", 40L);
        int searchRadius = getConfig().getInt("search-radius", 48);
        boolean onlyLoadedChunks = getConfig().getBoolean("only-loaded-chunks", true);

        new CompassUpdateTask(this, markerKey, searchRadius, onlyLoadedChunks)
                .runTaskTimer(this, interval, interval);
    }
}
