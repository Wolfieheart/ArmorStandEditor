package io.github.rypofalem.armorstandeditor.protections;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltAPI;

public class BoltProtection implements Protection {

    // Protection class for Bolt entity protections
    // https://modrinth.com/plugin/bolt

    private final boolean boltenabled;
    private final BoltAPI bolt = Bukkit.getServer().getServicesManager().load(BoltAPI.class);

    public BoltProtection() {boltenabled = Bukkit.getPluginManager().isPluginEnabled("Bolt");}

    public boolean checkPermission(Entity entity, Player player) {

        if (!boltenabled) return true;
        if (player.isOp() || player.hasPermission("bolt.admin")) return true;

        if (!bolt.isProtected(entity)) return true;

        return bolt.canAccess(entity, player, "interact");

     }

}
