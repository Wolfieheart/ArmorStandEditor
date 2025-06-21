package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;
import io.github.rypofalem.armorstandeditor.PlayerEditor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Map;

public class SizeMenu extends ASEHolder {

    public ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();
    Inventory menuInv;
    private Debug debug;
    private PlayerEditor pe;
    private ArmorStand as;
    static String name = "Size Menu";

    public SizeMenu(PlayerEditor pe, ArmorStand as) {
        this.pe = pe;
        this.as = as;
        this.debug = new Debug(pe.plugin);
        name = pe.plugin.getLang().getMessage("sizeMenu", "menutitle");
        menuInv = Bukkit.createInventory(pe.getManager().getSizeMenuHolder(), 27, name);
    }

    //Replace Values.
    final String VALUETOREPLACE = "§" + plugin.getLang().getFormat("info"); //VALUE WE DONT WANT: §6
    final String VALUEWEWANT = "§" +plugin.getLang().getFormat("iconname").substring(0, 1) +
            "§" + plugin.getLang().getFormat("iconname").substring(1); //VALUE WE WANT IS: §2§nScale = 9

    //Preset Strings.
    final String SCALE1 = plugin.getLang().getMessage("scale1").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE2 = plugin.getLang().getMessage("scale2").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE3 = plugin.getLang().getMessage("scale3").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE4 = plugin.getLang().getMessage("scale4").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE5 = plugin.getLang().getMessage("scale5").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE6 = plugin.getLang().getMessage("scale6").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE7 = plugin.getLang().getMessage("scale7").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE8 = plugin.getLang().getMessage("scale8").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE9 = plugin.getLang().getMessage("scale9").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALE10 = plugin.getLang().getMessage("scale10").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALEPLUS12 = plugin.getLang().getMessage("scaleadd12").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALEMINUS12 = plugin.getLang().getMessage("scaleremove12").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALEPLUS110 = plugin.getLang().getMessage("scaleadd110").replace(VALUETOREPLACE, VALUEWEWANT);
    final String SCALEMINUS110 = plugin.getLang().getMessage("scaleremove110").replace(VALUETOREPLACE, VALUEWEWANT);

    //Menu Stuff
    final String BACKTOMENU = plugin.getLang().getMessage("backtomenu").replace(VALUETOREPLACE, VALUEWEWANT);
    final String RESET = plugin.getLang().getMessage("reset").replace(VALUETOREPLACE, VALUEWEWANT);


    private void fillInventory() {
        menuInv.clear();
        ItemStack blankSlot = createIcon(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), "blankslot");
        ItemStack base10 = createIcon(new ItemStack(Material.RED_CONCRETE, 1), "scale1");
        ItemStack base20 = createIcon(new ItemStack(Material.RED_CONCRETE, 2), "scale2");
        ItemStack base30 = createIcon(new ItemStack(Material.RED_CONCRETE, 3), "scale3");
        ItemStack base40 = createIcon(new ItemStack(Material.RED_CONCRETE, 4), "scale4");
        ItemStack base50 = createIcon(new ItemStack(Material.RED_CONCRETE, 5), "scale5");
        ItemStack base60 = createIcon(new ItemStack(Material.RED_CONCRETE, 6), "scale6");
        ItemStack base70 = createIcon(new ItemStack(Material.RED_CONCRETE, 7), "scale7");
        ItemStack base80 = createIcon(new ItemStack(Material.RED_CONCRETE, 8), "scale8");
        ItemStack base90 = createIcon(new ItemStack(Material.RED_CONCRETE, 9), "scale9");
        ItemStack base100 = createIcon(new ItemStack(Material.RED_CONCRETE, 10), "scale10");
        ItemStack add12toBase = createIcon(new ItemStack(Material.ORANGE_CONCRETE, 1), "scaleadd12");
        ItemStack remove12fromBase = createIcon(new ItemStack(Material.GREEN_CONCRETE, 1), "scaleremove12");
        ItemStack add110fromBase = createIcon(new ItemStack(Material.ORANGE_CONCRETE, 2), "scaleadd110");
        ItemStack remove110fromBase = createIcon(new ItemStack(Material.GREEN_CONCRETE, 2), "scaleremove110");
        ItemStack backToMenu = createIcon(new ItemStack(Material.RED_WOOL, 1), "backtomenu");
        ItemStack resetIcon = createIcon(new ItemStack(Material.NETHER_STAR, 1), "reset");

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

    private ItemStack createIcon(ItemStack icon, String path, String option) {
        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.getPersistentDataContainer().set(ArmorStandEditorPlugin.instance().getIconKey(), PersistentDataType.STRING, "ase " + option);
        meta.setDisplayName(getIconName(path, option));
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(getIconDescription(path, option));
        meta.setLore(loreList);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        icon.setItemMeta(meta);
        return icon;
    }

    private String getIconName(String path, String option) {
        return pe.plugin.getLang().getMessage(path, "iconname", option);
    }


    private String getIconDescription(String path, String option) {
        return pe.plugin.getLang().getMessage(path + ".description", "icondescription", option);
    }


    public void handleAttributeScaling(String itemName, Player player) {
        if (itemName == null || player == null) return;

        // Separate maps for positive and negative scaling options
        Map<String, Double> positiveScaleMap = Map.ofEntries(
            Map.entry(SCALE1, 1.0),
            Map.entry(SCALE2, 2.0),
            Map.entry(SCALE3, 3.0),
            Map.entry(SCALE4, 4.0),
            Map.entry(SCALE5, 5.0),
            Map.entry(SCALE6, 6.0),
            Map.entry(SCALE7, 7.0),
            Map.entry(SCALE8, 8.0),
            Map.entry(SCALE9, 9.0),
            Map.entry(SCALE10, 10.0),
            Map.entry(SCALEPLUS12, 0.5),
            Map.entry(SCALEPLUS110, 0.1)
        );

        Map<String, Double> negativeScaleMap = Map.ofEntries(
            Map.entry(SCALEMINUS12, 0.5), // Changed value to negative for decrement
            Map.entry(SCALEMINUS110, 0.1) // Changed value to negative for decrement
        );

        if (positiveScaleMap.containsKey(itemName)) {
            handleScaleChange(player, itemName, positiveScaleMap.get(itemName));
        } else if (negativeScaleMap.containsKey(itemName)) {
            handleScaleChange(player, itemName, negativeScaleMap.get(itemName));
        } else if (itemName.equals(BACKTOMENU)) {
            handleBackToMenu(player);
        } else if (itemName.equals(RESET)) {
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
        setArmorStandScale(player, RESET, 1);
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

        if(!as.isValid()) return;

        if(!player.hasPermission("asedit.togglesize")) return;

        // Basically go from 0 directly to ItemSize
        if (itemName.equals(SCALE1) || itemName.equals(SCALE2) || itemName.equals(SCALE3)
                || itemName.equals(SCALE4) || itemName.equals(SCALE5) || itemName.equals(SCALE6)
                || itemName.equals(SCALE7) || itemName.equals(SCALE8) || itemName.equals(SCALE9)
                || itemName.equals(SCALE10)) {
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
        } else if (itemName.equals(SCALEPLUS12) || itemName.equals(SCALEPLUS110)) {
            currentScaleValue = as.getAttribute(Attribute.SCALE).getBaseValue(); //Get the current Value
            newScaleValue = currentScaleValue + scaleValue; // Add for increments
            debug.log("Result of the scale Calculation: " + newScaleValue);
            if (newScaleValue > plugin.getMaxScaleValue()) {
                pe.getPlayer().sendMessage(plugin.getLang().getMessage("scalemaxwarn", "warn"));
                return;
            }
            as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
            //Subtract either 0.1 or 0.5 from the current
        } else if (itemName.equals(SCALEMINUS12) || itemName.equals(SCALEMINUS110)) {
            currentScaleValue = as.getAttribute(Attribute.SCALE).getBaseValue();
            newScaleValue = currentScaleValue - scaleValue; // Subtract for decrements
            debug.log("Result of the scale Calculation: " + newScaleValue);
            if (newScaleValue < plugin.getMinScaleValue()) {
                pe.getPlayer().sendMessage(plugin.getLang().getMessage("scaleminwarn", "warn"));
                return;
            }
            as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
        } else if (itemName.equals(RESET)) { // Set it back to 1
            newScaleValue = 1.0;
            as.getAttribute(Attribute.SCALE).setBaseValue(newScaleValue);
        }
    }

    public void openMenu() {
        if (pe.getPlayer().hasPermission("asedit.togglesize")) {
            fillInventory();
            debug.log("Player '" + pe.getPlayer().getDisplayName() + "' has opened the Sizing Attribute Menu");
            pe.getPlayer().openInventory(menuInv);
        }
    }
}
