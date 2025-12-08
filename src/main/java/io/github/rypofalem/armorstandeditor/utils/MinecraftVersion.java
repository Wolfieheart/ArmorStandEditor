package io.github.rypofalem.armorstandeditor.utils;

import org.bukkit.Bukkit;

public class MinecraftVersion {

    private MinecraftVersion() {}

    public static final String MINECRAFT_1_17 = VersionUtil.fromString("1.17");
    public static final String MINECRAFT_1_18_2 = VersionUtil.fromString("1.18.2");
    public static final String MINECRAFT_1_19_1 = VersionUtil.fromString("1.19.1");
    public static final String MINECRAFT_1_19_3 = VersionUtil.fromString("1.19.3");
    public static final String MINECRAFT_1_20 = VersionUtil.fromString("1.20");
    public static final String MINECRAFT_1_20_2 = VersionUtil.fromString("1.20.2");
    public static final String MINECRAFT_1_20_3 = VersionUtil.fromString("1.20.3");
    public static final String MINECRAFT_1_20_5 = VersionUtil.fromString("1.20.5");
    public static final String MINECRAFT_1_21 = VersionUtil.fromString("1.21");
    public static final String MINECRAFT_1_21_2 = VersionUtil.fromString("1.21.2");
    public static final String MINECRAFT_1_21_3 = VersionUtil.fromString("1.21.3");
    public static final String MINECRAFT_1_21_5 = VersionUtil.fromString("1.21.5");
    public static final String MINECRAFT_1_21_9 = VersionUtil.fromString("1.21.9");

    public static final String CURRENT_Version = VersionUtil.fromString(Bukkit.getMinecraftVersion());
    public static final String OLDEST_Version_SUPPORTED = MINECRAFT_1_17;
}
