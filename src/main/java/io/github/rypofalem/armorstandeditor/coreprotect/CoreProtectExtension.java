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

    private final ArmorStandEditorPlugin plugin;
    private final boolean enabled;

    public CoreProtectExtension(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getServer().getPluginManager().isPluginEnabled("CoreProtect");
    }

    public void logChange(Player player, ArmorStand armorStand, @NotNull ItemStack[] oldContents, @NotNull ItemStack[] newContents) {
        if (!enabled) return;
        try {
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
        } catch (Exception exception) {
            plugin.getSLF4JLogger().warn("Error/Exception while logging with CoreProtect", exception);
        }
    }
}