package io.github.rypofalem.armorstandeditor.utils;

import org.bukkit.Bukkit;

public class MinecraftVersion {

    private MinecraftVersion() {}

    public static final VersionUtil MINECRAFT_1_17 = VersionUtil.fromString("1.17");
    public static final VersionUtil MINECRAFT_1_21 = VersionUtil.fromString("1.21");
    public static final VersionUtil MINECRAFT_1_21_3 = VersionUtil.fromString("1.21.3");

    public static final VersionUtil CURRENT_Version = VersionUtil.fromString(Bukkit.getMinecraftVersion());
    public static final VersionUtil OLDEST_Version_SUPPORTED = MINECRAFT_1_17;
}
