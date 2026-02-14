package io.github.rypofalem.armorstandeditor.utils;


import org.bukkit.Bukkit;

public class MinecraftVersion {

    private MinecraftVersion() {
    }

    public static final VersionUtil MINECRAFT_1_17 = VersionUtil.fromString("1.17");
    public static final VersionUtil MINECRAFT_1_20_4 = VersionUtil.fromString("1.20.4");
    public static final VersionUtil MINECRAFT_1_21 = VersionUtil.fromString("1.21");

    public static final VersionUtil CURRENT_VERSION = VersionUtil.fromString(Bukkit.getMinecraftVersion());
    public static final VersionUtil OLDEST_SUPPORTED_VERSION = MINECRAFT_1_17;
}
