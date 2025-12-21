package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;
import io.github.rypofalem.armorstandeditor.PlayerEditor;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class SizeMenu extends ASEHolder {

    public ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();
    Inventory menuInv;
    private Debug debug;
    private PlayerEditor pe;
    private ArmorStand as;
    static Component name;

    public SizeMenu(PlayerEditor pe, ArmorStand as) {
        this.pe = pe;
        this.as = as;
        this.debug = pe.plugin.debug;
        name = pe.plugin.getLang().getMessage("sizeMenu", "menutitle");
        menuInv = Bukkit.createInventory(pe.getManager().getSizeMenuHolder(), 27, name);
    }

    private void fillInventory() {
        menuInv.clear();
        ItemStack blankSlot = createIcon(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE, 1), "blankslot");
        ItemStack base10 = createIcon(ItemStack.of(Material.RED_CONCRETE, 1), "scale1");
        ItemStack base20 = createIcon(ItemStack.of(Material.RED_CONCRETE, 2), "scale2");
        ItemStack base30 = createIcon(ItemStack.of(Material.RED_CONCRETE, 3), "scale3");
        ItemStack base40 = createIcon(ItemStack.of(Material.RED_CONCRETE, 4), "scale4");
        ItemStack base50 = createIcon(ItemStack.of(Material.RED_CONCRETE, 5), "scale5");
        ItemStack base60 = createIcon(ItemStack.of(Material.RED_CONCRETE, 6), "scale6");
        ItemStack base70 = createIcon(ItemStack.of(Material.RED_CONCRETE, 7), "scale7");
        ItemStack base80 = createIcon(ItemStack.of(Material.RED_CONCRETE, 8), "scale8");
        ItemStack base90 = createIcon(ItemStack.of(Material.RED_CONCRETE, 9), "scale9");
        ItemStack base100 = createIcon(ItemStack.of(Material.RED_CONCRETE, 10), "scale10");
        ItemStack add12toBase = createIcon(ItemStack.of(Material.ORANGE_CONCRETE, 1), "scaleadd12");
        ItemStack remove12fromBase = createIcon(ItemStack.of(Material.GREEN_CONCRETE, 1), "scaleremove12");
        ItemStack add110fromBase = createIcon(ItemStack.of(Material.ORANGE_CONCRETE, 2), "scaleadd110");
        ItemStack remove110fromBase = createIcon(ItemStack.of(Material.GREEN_CONCRETE, 2), "scaleremove110");
        ItemStack backToMenu = createIcon(ItemStack.of(Material.RED_WOOL, 1), "backtomenu");
        ItemStack resetIcon = createIcon(ItemStack.of(Material.NETHER_STAR, 1), "reset");

        ItemStack[] items = {
            backToMenu, blankSlot, base10, base20, base30, base40, base50, base60, blankSlot,
            resetIcon, blankSlot, base70, base80, base90, base100, blankSlot, add12toBase, remove12fromBase,
            blankSlot, blankSlot, blankSlot, blankSlot, blankSlot, blankSlot, blankSlot, add110fromBase, remove110fromBase
        };

        menuInv.setContents(items);
    }

    private ItemStack createIcon(ItemStack icon, String path) {
        return createIcon(icon, path, null);
    }

    @SuppressWarnings("UnstableApiUsage")
    private ItemStack createIcon(ItemStack icon, String path, String option) {
        icon.setData(DataComponentTypes.CUSTOM_NAME, getIconName(path, option));
        icon.editPersistentDataContainer(pdc -> pdc.set(pe.plugin.getIconKey(), PersistentDataType.STRING, path));
        icon.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(getIconDescription(path, option)).build());
        icon.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
            .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS).build());
        return icon;

    }

    private Component getIconName(String path, String option) {
        return pe.plugin.getLang().getMessage(path, "iconname", option);
    }


    private Component getIconDescription(String path, String option) {
        return pe.plugin.getLang().getMessage(path + ".description", "icondescription", option);
    }


    public void handleAttributeScaling(String itemName, Player player) {
        if (itemName == null || player == null) return;

        // Separate maps for positive and negative scaling options
        Map<String, Double> positiveScaleMap = Map.ofEntries(
            Map.entry("scale1", 1.0),
            Map.entry("scale2", 2.0),
            Map.entry("scale3", 3.0),
            Map.entry("scale4", 4.0),
            Map.entry("scale5", 5.0),
            Map.entry("scale6", 6.0),
            Map.entry("scale7", 7.0),
            Map.entry("scale8", 8.0),
            Map.entry("scale9", 9.0),
            Map.entry("scale10", 10.0),
            Map.entry("scaleadd12", 0.5),
            Map.entry("scaleadd110", 0.1)
        );

        Map<String, Double> negativeScaleMap = Map.ofEntries(
            Map.entry("scaleremove12", 0.5), // value used for decrement
            Map.entry("scaleremove110", 0.1)
        );

        if (positiveScaleMap.containsKey(itemName)) {
            handleScaleChange(player, itemName, positiveScaleMap.get(itemName));
        } else if (negativeScaleMap.containsKey(itemName)) {
            handleScaleChange(player, itemName, negativeScaleMap.get(itemName));
        } else if (itemName.equals("backtomenu")) {
            handleBackToMenu(player);
        } else if (itemName.equals("reset")) {
            handleReset(player);
        }
    }


    private void handleScaleChange(Player player, String itemName, double scale) {
        setArmorStandScale(player, itemName, scale);
        playChimeSound(player);
        player.closeInventory();
    }

    private void handleBackToMenu(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
        player.closeInventory();
        pe.openMenu();
    }

    private void handleReset(Player player) {
        setArmorStandScale(player, "reset", 1);
        playChimeSound(player);
        player.closeInventory();
    }

    private void playChimeSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
    }

    private void setArmorStandScale(Player player, String itemName, double scaleValue) {
        debug.log("Setting the Scale of the ArmorStand");
        double currentScaleValue = 0;
        double newScaleValue;

        if (!as.isValid()) return;

        if (!player.hasPermission("asedit.togglesize")) return;

        // Basically go from 0 directly to ItemSize
        if (itemName.equals("scale1") || itemName.equals("scale2") || itemName.equals("scale3")
            || itemName.equals("scale4") || itemName.equals("scale5") || itemName.equals("scale6")
            || itemName.equals("scale7") || itemName.equals("scale8") || itemName.equals("scale9")
            || itemName.equals("scale10")) {
            newScaleValue = currentScaleValue + scaleValue;
            debug.log("Result of the scale Calculation: " + newScaleValue);

            if (newScaleValue > plugin.getMaxScaleValue()) {
                pe.getPlayer().sendMessage(plugin.getLang().getMessage("scalemaxwarn", "warn"));
            } else if (newScaleValue < plugin.getMinScaleValue()) {
                pe.getPlayer().sendMessage(plugin.getLang().getMessage("scaleminwarn", "warn"));
            } else {
                as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
            }

            // Add either 0.1 or 0.5 to the current
        } else if (itemName.equals("scaleadd12") || itemName.equals("scaleadd110")) {
            currentScaleValue = as.getAttribute(Attribute.SCALE).getBaseValue(); //Get the current Value
            newScaleValue = currentScaleValue + scaleValue; // Add for increments
            debug.log("Result of the scale Calculation: " + newScaleValue);
            if (newScaleValue > plugin.getMaxScaleValue()) {
                pe.getPlayer().sendMessage(plugin.getLang().getMessage("scalemaxwarn", "warn"));
                return;
            }
            as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
            //Subtract either 0.1 or 0.5 from the current
        } else if (itemName.equals("scaleremove12") || itemName.equals("scaleremove110")) {
            currentScaleValue = as.getAttribute(Attribute.SCALE).getBaseValue();
            newScaleValue = currentScaleValue - scaleValue; // Subtract for decrements
            debug.log("Result of the scale Calculation: " + newScaleValue);
            if (newScaleValue < plugin.getMinScaleValue()) {
                pe.getPlayer().sendMessage(plugin.getLang().getMessage("scaleminwarn", "warn"));
                return;
            }
            as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
        } else if (itemName.equals("reset")) { // Set it back to 1
            newScaleValue = 1.0;
            as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
        }
    }

    public void openMenu() {
        if (pe.getPlayer().hasPermission("asedit.togglesize")) {
            fillInventory();
            debug.log("Player '" + pe.getPlayer().getName() + "' has opened the Sizing Attribute Menu");
            pe.getPlayer().openInventory(menuInv);
        }
    }
}
