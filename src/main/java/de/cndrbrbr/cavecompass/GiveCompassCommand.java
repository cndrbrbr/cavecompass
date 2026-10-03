package de.cndrbrbr.cavecompass;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@code /cavecompass [cave|<block>] [radius] [count]}
 *
 * Holding a Cave Compass in the main hand reconfigures that compass;
 * otherwise a new one with the given settings is handed out.
 */
public final class GiveCompassCommand implements TabExecutor {

    private final CaveCompassItem compassItem;
    private final CompassSettings defaults;
    private final int maxRadius;
    private final int defaultClusterSize;

    public GiveCompassCommand(CaveCompassItem compassItem, CompassSettings defaults, int maxRadius, int defaultClusterSize) {
        this.compassItem = compassItem;
        this.defaults = defaults;
        this.maxRadius = maxRadius;
        this.defaultClusterSize = defaultClusterSize;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        CompassSettings settings;
        try {
            boolean blockMode = args.length >= 1 && !args[0].equalsIgnoreCase(CompassSettings.CAVE);
            settings = CompassSettings.parse(
                    args,
                    defaults.radius(),
                    maxRadius,
                    blockMode ? defaultClusterSize : 1,
                    GiveCompassCommand::resolveBlock
            );
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c" + e.getMessage());
            player.sendMessage("§7Usage: /" + label + " [cave|<block>] [radius] [count]");
            return true;
        }

        String summary = describe(settings);
        ItemStack held = player.getInventory().getItemInMainHand();
        if (compassItem.isCaveCompass(held)) {
            compassItem.configure(held, settings);
            player.getInventory().setItemInMainHand(held);
            player.sendMessage("§bCompass now searches for " + summary + ".");
            return true;
        }

        var leftover = player.getInventory().addItem(compassItem.create(settings));
        if (leftover.isEmpty()) {
            player.sendMessage("§bYou received a compass searching for " + summary + ".");
        } else {
            player.sendMessage("§cYour inventory is full — no room for a compass.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            if (CompassSettings.CAVE.startsWith(prefix)) {
                out.add(CompassSettings.CAVE);
            }
            for (Material m : Material.values()) {
                if (isSearchableBlock(m)) {
                    String key = m.getKey().getKey();
                    if (key.startsWith(prefix)) {
                        out.add(key);
                    }
                }
            }
        } else if (args.length == 2) {
            for (String r : List.of("32", "64", "96", "128")) {
                if (r.startsWith(args[1]) && Integer.parseInt(r) <= maxRadius) {
                    out.add(r);
                }
            }
        } else if (args.length == 3) {
            out.addAll(List.of("1", "2", "3", "5"));
        }
        return out;
    }

    private static String resolveBlock(String input) {
        Material m = Material.matchMaterial(input);
        return m != null && isSearchableBlock(m) ? m.getKey().getKey() : null;
    }

    private static boolean isSearchableBlock(Material m) {
        return m.isBlock() && !m.isAir() && !m.name().startsWith("LEGACY_");
    }

    private static String describe(CompassSettings s) {
        if (s.isCave()) {
            return "caves (radius " + s.radius() + ")";
        }
        String count = s.clusterSize() > 1 ? "groups of " + s.clusterSize() + "+ " : "";
        return count + s.targetName() + " (radius " + s.radius() + ")";
    }
}
