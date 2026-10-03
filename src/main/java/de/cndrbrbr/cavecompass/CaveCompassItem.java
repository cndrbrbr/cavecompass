package de.cndrbrbr.cavecompass;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Creates and identifies Cave Compass items, and points them at a target
 * location using the same "lodestone tracker" mechanism vanilla lodestone
 * compasses use — {@link CompassMeta#setLodestone(Location)} with tracking
 * disabled lets a compass point at an arbitrary, moving coordinate with no
 * actual lodestone block involved. Because this is purely coordinate-based,
 * it points correctly at a target the player cannot see or hear, above or
 * below them, through solid rock — which is exactly what "underground,
 * pointing at a cave I can't see" needs.
 *
 * Each compass carries its own {@link CompassSettings} (what to search for,
 * radius, cluster size) in its persistent data. Compasses without stored
 * settings — e.g. ones handed out by older plugin versions — are cave
 * compasses using the configured defaults.
 */
public final class CaveCompassItem {

    private final NamespacedKey markerKey;
    private final NamespacedKey targetKey;
    private final NamespacedKey radiusKey;
    private final NamespacedKey clusterKey;

    public CaveCompassItem(Plugin plugin) {
        this.markerKey = new NamespacedKey(plugin, "tracker");
        this.targetKey = new NamespacedKey(plugin, "target");
        this.radiusKey = new NamespacedKey(plugin, "radius");
        this.clusterKey = new NamespacedKey(plugin, "cluster");
    }

    public ItemStack create(CompassSettings settings) {
        ItemStack item = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setLodestoneTracked(false);
        meta.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        configure(item, settings);
        return item;
    }

    public boolean isCaveCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(markerKey, PersistentDataType.BYTE);
    }

    /** Stores new search settings on an existing compass and resets its display. */
    public void configure(ItemStack item, CompassSettings settings) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(targetKey, PersistentDataType.STRING, settings.target());
        data.set(radiusKey, PersistentDataType.INTEGER, settings.radius());
        data.set(clusterKey, PersistentDataType.INTEGER, settings.clusterSize());

        meta.setDisplayName(settings.isCave() ? "§bCave Compass" : "§eCompass: " + capitalize(settings.targetName()));
        meta.setLore(lore(settings, "§7Calculating..."));
        item.setItemMeta(meta);
    }

    public CompassSettings settingsOf(ItemStack item, CompassSettings defaults) {
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        return new CompassSettings(
                data.getOrDefault(targetKey, PersistentDataType.STRING, defaults.target()),
                data.getOrDefault(radiusKey, PersistentDataType.INTEGER, defaults.radius()),
                data.getOrDefault(clusterKey, PersistentDataType.INTEGER, defaults.clusterSize())
        );
    }

    /**
     * Points the compass at the target's horizontal direction (via the
     * needle) and, since a needle alone cannot show altitude, also updates
     * the item's lore with a vertical hint — together giving full 3D
     * guidance to a target the player can't see or hear.
     *
     * @param deltaY target Y minus player Y (positive = target is above)
     */
    public void pointAt(ItemStack item, CompassSettings settings, Location target, int deltaY) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setLodestoneTracked(false);
        meta.setLodestone(target);
        meta.setLore(lore(settings, VerticalHint.describe(deltaY)));
        item.setItemMeta(meta);
    }

    /** Shows that nothing was found within the radius; the needle keeps its last direction. */
    public void markNotFound(ItemStack item, CompassSettings settings) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setLore(lore(settings, "§cNothing found within " + settings.radius() + " blocks"));
        item.setItemMeta(meta);
    }

    private static List<String> lore(CompassSettings settings, String statusLine) {
        String what = settings.isCave()
                ? "the nearest cave"
                : settings.clusterSize() > 1
                        ? "a group of " + settings.clusterSize() + "+ " + settings.targetName() + " blocks"
                        : "the nearest " + settings.targetName();
        return List.of(
                "§7Points toward " + what + ".",
                "§7Search radius: " + settings.radius() + " blocks",
                statusLine
        );
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
