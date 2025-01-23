package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.PlayerEditor;
import io.github.rypofalem.armorstandeditor.devtools.Debug;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class MainMenu implements EditorMenu{

    private final Inventory menuInv;
    private final PlayerEditor pe;
    private String name;
    private Debug debug;

    public MainMenu(Inventory menuInv, PlayerEditor pe) {
        this.menuInv = menuInv;
        this.pe = pe;
        name = pe.plugin.getLanguage().getMessage("mainmenutitle","menutitle");
        menuInv = Bukkit.createInventory(pe.getPlayer(),54,name);

    }

    @Override
    public void open() {

    }

    @Override
    public void fillInventory() {

    }

    @Override
    public ArmorStand getArmorStand() {
        return null;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
