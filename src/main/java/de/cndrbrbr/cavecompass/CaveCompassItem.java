package de.cndrbrbr.cavecompass;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;

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
 */
public final class CaveCompassItem {

    private CaveCompassItem() {
    }

    public static ItemStack create(NamespacedKey markerKey) {
        ItemStack item = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) item.getItemMeta();

        meta.setDisplayName("§bCave Compass");
        meta.setLore(List.of(
                "§7Points toward the nearest cave.",
                "§7Works underground, even out of sight."
        ));
        meta.setLodestoneTracked(false);
        meta.getPersistentDataContainer().set(markerKey, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isCaveCompass(NamespacedKey markerKey, ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(markerKey, PersistentDataType.BYTE);
    }

    public static void pointAt(ItemStack item, Location target) {
        CompassMeta meta = (CompassMeta) item.getItemMeta();
        meta.setLodestoneTracked(false);
        meta.setLodestone(target);
        item.setItemMeta(meta);
    }
}
