package io.github.rypofalem.armorstandeditor.protections;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.popcraft.bolt.BoltAPI;

public class BoltProtection implements Protection {

    // Protection class for Bolt entity protections
    // https://modrinth.com/plugin/bolt

    private final boolean boltenabled;
    private Debug debug;
    private ArmorStandEditorPlugin plugin;

    public BoltProtection() {
        boltenabled = Bukkit.getPluginManager().isPluginEnabled("Bolt");
        this.debug = new Debug(plugin);
        debug.log("Initialized Bolt Protection for ASE. Bolt Protection is " + (boltenabled ? "enabled." : "disabled."));
        this.plugin = ArmorStandEditorPlugin.instance();
    }

    public boolean checkPermission(Entity entity, Player player) {
        if (!boltenabled || player.isOp() || player.hasPermission("asedit.ignoreProtection.bolt")) return true;
        debug.log("Checking Bolt Protection for player " + player.getName() + " on entity " + entity.getUniqueId());

        //Failsafe: Only use the API if the plugin is enabled, otherwise it will throw an error
        BoltAPI boltAPI = Bukkit.getServer().getServicesManager().load(BoltAPI.class);
        if(boltAPI == null) return true;
        if (!boltAPI.isProtected(entity)) return true;

        return boltAPI.canAccess(entity, player, "interact");
     }

}
