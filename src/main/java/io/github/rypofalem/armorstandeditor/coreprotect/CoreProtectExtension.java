package io.github.rypofalem.armorstandeditor.coreprotect;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import net.coreprotect.config.Config;
import net.coreprotect.listener.player.PlayerInteractEntityListener;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CoreProtectExtension {
    private static ArmorStandEditorPlugin plugin;

    public static void init(ArmorStandEditorPlugin plugin) {
        CoreProtectExtension.plugin = plugin;
    }

    public static void logChange(Player player, ArmorStand armorStand, @NotNull ItemStack[] oldContents, @NotNull ItemStack[] newContents) {
        if (plugin == null || !plugin.getServer().getPluginManager().isPluginEnabled("CoreProtect")) return;
        try { // As this is unstable due to being copied from net.coreprotect.listener.player.ArmorStandManipulateListener, it is prone to errors with updates
            if (!Config.getConfig(player.getWorld()).ITEM_TRANSACTIONS) {
                return;
            }
            for (int i = 0; i < oldContents.length; i++) {
                ItemStack oldItem = oldContents[i];
                ItemStack newItem = newContents[i];
                if (oldItem.equals(newItem)) continue;

                if (!oldItem.getType().isAir()) {
                    oldContents[i] = oldItem.clone();
                }
                if (!newItem.getType().isAir()) {
                    newContents[i] = newItem.clone();
                }
            }

            PlayerInteractEntityListener.queueContainerSpecifiedItems(player.getName(), Material.ARMOR_STAND, new Object[]{oldContents, newContents}, armorStand.getLocation(), false);
        } catch (Throwable throwable) {
            plugin.getSLF4JLogger().warn("Error/Exception while logging with CoreProtect", throwable);
        }
    }
}
