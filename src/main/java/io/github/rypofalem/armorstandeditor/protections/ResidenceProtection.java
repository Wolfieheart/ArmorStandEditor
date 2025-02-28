package io.github.rypofalem.armorstandeditor.protections;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ResidenceProtection implements Protection {
    private final boolean resEnabled;
    private final Residence resInstance;

    public ResidenceProtection() {
        resEnabled = Bukkit.getPluginManager().isPluginEnabled("Residence");
        if (Bukkit.getPluginManager().isPluginEnabled("Residence")) {
            resInstance = null;
            return;
        }
        resInstance = Residence.getInstance();
    }

    @Override
    public boolean checkPermission(Block block, Player player) {
        if (!resEnabled) return true;
        if (player.isOp()) return true;
        if (player.hasPermission("asedit.ignoreProtection.residence")) return true; //Add Additional Permission

        final Location eLocation = block.getLocation();
        final ClaimedResidence residence = resInstance.getResidenceManager().getByLoc(eLocation);
        if (residence == null) return true;

        ResidencePermissions perms = residence.getPermissions();
        boolean hasPermission = perms.playerHas(player.getName(), "build", true);
        if (!hasPermission) resInstance.msg(player, lm.Flag_Deny, Flags.build);
        return hasPermission;
    }
}
