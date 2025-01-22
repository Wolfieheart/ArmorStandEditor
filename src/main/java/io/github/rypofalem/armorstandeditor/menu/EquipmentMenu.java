package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.PlayerEditor;
import io.github.rypofalem.armorstandeditor.api.EquipmentMenuOpenedEvent;
import io.github.rypofalem.armorstandeditor.devtools.Debug;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static net.kyori.adventure.text.Component.text;

public class EquipmentMenu implements EditorMenu {

    Inventory equipMenu;
    private PlayerEditor pe;
    private ArmorStand armorStand;
    String menuName;
    public Debug debug;

    public EquipmentMenu(PlayerEditor pe, ArmorStand as){
        this.pe = pe;
        this.armorStand = as;
        menuName = pe.plugin.getLanguage().getMessage("equiptitle", "menutitle");
        this.debug = new Debug(pe.plugin);
        equipMenu = Bukkit.createInventory(this, 18, text(menuName));
    }

    @Override
    public void open() {

        EquipmentMenuOpenedEvent event = new EquipmentMenuOpenedEvent(pe.getPlayer(), this);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;

        if(pe.getPlayer().hasPermission("asedit.equipment")) {
            fillInventory();
            debug.log("Player '" + pe.getPlayer().getName() + "' has opened the Equipment Menu.");
            pe.getPlayer().openInventory(this.equipMenu);
        }
    }

    @Override
    public void fillInventory() {
        equipMenu.clear();
        EntityEquipment equipment = armorStand.getEquipment();
        ItemStack helmet = equipment.getHelmet();
        ItemStack chest = equipment.getChestplate();
        ItemStack pants = equipment.getLeggings();
        ItemStack feetsies = equipment.getBoots();
        ItemStack rightHand = equipment.getItemInMainHand();
        ItemStack leftHand = equipment.getItemInOffHand();
        equipment.clear();

        ItemStack disabledIcon = new ItemStack(Material.BARRIER);
        ItemMeta meta = disabledIcon.getItemMeta();
        meta.displayName(text(pe.plugin.getLanguage().getMessage("disabled", "warn"))); //equipslot.msg <option>
        meta.getPersistentDataContainer().set(pe.plugin.getIconKey(), PersistentDataType.STRING, "ase icon"); // mark as icon
        disabledIcon.setItemMeta(meta);

        ItemStack helmetIcon = createIcon(Material.LEATHER_HELMET, "helm");
        ItemStack chestIcon = createIcon(Material.LEATHER_CHESTPLATE, "chest");
        ItemStack pantsIcon = createIcon(Material.LEATHER_LEGGINGS, "pants");
        ItemStack feetsiesIcon = createIcon(Material.LEATHER_BOOTS, "boots");
        ItemStack rightHandIcon = createIcon(Material.WOODEN_SWORD, "rhand");
        ItemStack leftHandIcon = createIcon(Material.SHIELD, "lhand");
        ItemStack[] items =
                {helmetIcon, chestIcon, pantsIcon, feetsiesIcon, rightHandIcon, leftHandIcon, disabledIcon, disabledIcon, disabledIcon,
                        helmet, chest, pants, feetsies, rightHand, leftHand, disabledIcon, disabledIcon, disabledIcon
                };
        equipMenu.setContents(items);
    }

    private ItemStack createIcon(Material mat, String slot) {
        ItemStack icon = new ItemStack(mat);
        ItemMeta meta = icon.getItemMeta();
        meta.getPersistentDataContainer().set(pe.plugin.getIconKey(), PersistentDataType.STRING, "ase icon");
        meta.displayName(text(pe.plugin.getLanguage().getMessage("equipslot", "iconname", slot))); //equipslot.msg <option>
        ArrayList<Component> loreList = new ArrayList<>();
        loreList.add(text(pe.plugin.getLanguage().getMessage("equipslot.description", "icondescription", slot))); //equioslot.description.msg <option>
        meta.lore(loreList);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        icon.setItemMeta(meta);
        return icon;
    }


    @Override
    public ArmorStand getArmorStand() {
        return this.armorStand;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return equipMenu;
    }
}
