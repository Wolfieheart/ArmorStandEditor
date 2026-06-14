package io.github.rypofalem.armorstandeditor;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class TestHelperFunctions {

    public static ItemStack itemOf(Material material) {
        ItemStack item = new ItemStack(material);
        item.setItemMeta(item.getItemMeta()); // force MockBukkit to initialise meta
        return item;
    }

    public static ItemStack itemWithName(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack itemWithLore(Material material, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.lore(lore.stream()
                .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                .collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ItemStack itemWithCustomModelData(Material material, float modelData) {
        CustomModelDataComponent component = mock(CustomModelDataComponent.class);
        when(component.getFloats()).thenReturn(List.of(modelData));

        ItemMeta meta = mock(ItemMeta.class);
        when(meta.hasCustomModelDataComponent()).thenReturn(true);
        when(meta.getCustomModelDataComponent()).thenReturn(component);

        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(material);
        when(item.getItemMeta()).thenReturn(meta);
        return item;
    }
    public static ItemStack fullyDecoratedItem(Material material, String legacyName, List<String> legacyLore, int modelData) {
        ItemStack item = itemOf(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(legacyName));
        meta.lore(legacyLore.stream()
                .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
                .collect(Collectors.toList()));
        meta.setCustomModelData(modelData);
        item.setItemMeta(meta);
        return item;
    }

    public static ArmorStand mockArmorStand(Location location){
        ArmorStand stand = mock(ArmorStand.class);
        when(stand.getUniqueId()).thenReturn(UUID.randomUUID());
        when(stand.getLocation()).thenReturn(location);
        return stand;
    }

    public static ItemFrame mockItemFrame(Location location) {
        ItemFrame frame = mock(ItemFrame.class);
        when(frame.getUniqueId()).thenReturn(UUID.randomUUID());
        when(frame.getLocation()).thenReturn(location);
        return frame;
    }

    public static GlowItemFrame mockGlowItemFrame(Location location) {
        GlowItemFrame frame = mock(GlowItemFrame.class);
        when(frame.getUniqueId()).thenReturn(UUID.randomUUID());
        when(frame.getLocation()).thenReturn(location);
        return frame;
    }

}
