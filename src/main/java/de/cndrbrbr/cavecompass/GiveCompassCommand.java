package de.cndrbrbr.cavecompass;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GiveCompassCommand implements CommandExecutor {

    private final NamespacedKey markerKey;

    public GiveCompassCommand(NamespacedKey markerKey) {
        this.markerKey = markerKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        ItemStack compass = CaveCompassItem.create(markerKey);
        var leftover = player.getInventory().addItem(compass);

        if (leftover.isEmpty()) {
            player.sendMessage("§bYou received a Cave Compass.");
        } else {
            player.sendMessage("§cYour inventory is full — no room for a Cave Compass.");
        }

        return true;
    }
}
