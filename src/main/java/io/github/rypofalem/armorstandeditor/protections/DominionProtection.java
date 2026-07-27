package io.github.rypofalem.armorstandeditor.protections;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import cn.lunadeer.dominion.api.dtos.flag.PriFlag;
import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;

import static cn.lunadeer.dominion.api.dtos.flag.Flags.PLACE;
import static org.bukkit.Material.*;

public class DominionProtection implements Protection {

    private Debug debug;
    private ArmorStandEditorPlugin plugin;


    public DominionProtection() {
        plugin = ArmorStandEditorPlugin.instance();
        debug = plugin.debug;
    }

    @Override
    public boolean checkPermission(Entity entity, Player player) {
        boolean domEnabled = Bukkit.getPluginManager().isPluginEnabled("Dominion");
        if (!domEnabled || player.isOp() || player.hasPermission("asedit.ignoreProtection.dominion")) return true;
        debug.log("Checking Dominion Protection for player " + player.getName() + " on entity " + entity.getUniqueId());

        Location loc = entity.getLocation();

        // -- Entity Check --
        Material entityBeingEditedMaterial;
        PriFlag flagToCheck;

        if(entity instanceof ArmorStand){
            entityBeingEditedMaterial = ARMOR_STAND;
            flagToCheck = Flags.BREAK_BLOCK;
        } else if (entity instanceof ItemFrame) {
            entityBeingEditedMaterial = entity instanceof GlowItemFrame ? GLOW_ITEM_FRAME : ITEM_FRAME;
            flagToCheck = Flags.ITEM_FRAME_INTERACTIVE;
        } else {
            return true;
        }
        debug.log("Editing " + entityBeingEditedMaterial + ": " + entity.getUniqueId());
        debug.log("Flag to check: " + flagToCheck);

        // checkPrivilegeFlagSilence resolves the dominion at the location itself (or falls back
        // to world-wide default permissions if there is no dominion there), so no wilderness
        // branch is needed the way Towny required one.
        boolean allowed = DominionAPI.getInstance().checkPrivilegeFlagSilence(loc, flagToCheck, player);
        debug.log("Is the edit going to be allowed?: " + allowed);
        if (!allowed) {
            player.sendMessage(plugin.getLang().getMessage("dominionNoEdit", "warn"));
        }
        return allowed;
    }
}