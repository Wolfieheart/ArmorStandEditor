/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016-2023  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.coreprotect.CoreProtectExtension;
import io.github.rypofalem.armorstandeditor.language.Language;
import io.github.rypofalem.armorstandeditor.utils.MinecraftVersion;
import io.github.rypofalem.armorstandeditor.utils.VersionUtil;
import io.github.rypofalem.armorstandeditor.Metrics.DrilldownPie;
import io.github.rypofalem.armorstandeditor.Metrics.SimplePie;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import static net.kyori.adventure.text.format.NamedTextColor.RED;


public class ArmorStandEditorPlugin extends JavaPlugin {

    //!!! DO NOT REMOVE THESE UNDER ANY CIRCUMSTANCES - Required for BStats and UpdateChecker !!!
    private static final int PLUGIN_ID = 12668;             //Used for BStats Metrics
    public final Debug debug = new Debug(this);

    private NamespacedKey iconKey;
    private static ArmorStandEditorPlugin instance;
    private Language lang;
    private CoreProtectExtension coreProtectExtension;

    //Server Version Detection: Paper or Spigot and Invalid NMS Version
    String nmsVersion;
    String languageFolderLocation = "lang/";
    String warningMCVer = "Minecraft Version: ";
    public boolean hasPaper = false;
    public boolean hasFolia = false;
    String nmsVersionNotLatest = null;
    String versionLogPrefix;

    //Hardcode the ASE Version
    public static final String ASE_VERSION = "26.1.2-51";
    public static final String SEPARATOR_FIELD = "================================";

    public PlayerEditorManager editorManager;

    //Output for Updates
    boolean opUpdateNotification = false;
    boolean runTheUpdateChecker = false;
    double updateCheckerInterval;

    //Edit Tool Information
    Material editTool;
    String toolType;
    int editToolData = Integer.MIN_VALUE;
    boolean requireToolData = false;
    boolean requireToolName = false;
    String editToolNameRaw = null;
    Component editToolName = null;
    boolean requireToolLore = false;
    List<?> editToolLore = null;
    boolean enablePerWorld = false;
    List<?> allowedWorldList = null;
    double maxScaleValue;
    double minScaleValue;
    double maxResetRange;

    //Custom Data Model Support - Readded
    boolean allowCustomModelData = false;
    // FIX: renamed from customModelDataInt and retyped to int — it was float but used as an integer throughout
    int customModelDataValue;

    //GUI Settings
    boolean requireSneaking = false;
    boolean sendToActionBar = true;

    //Armor Stand Specific Settings
    double coarseRot;
    double fineRot;
    boolean glowItemFrames = false;
    boolean invisibleItemFrames = true;
    boolean armorStandVisibility = true;
    boolean defaultGravity = false;

    //Misc Options
    boolean allowedToRetrieveOwnPlayerHead = false;
    boolean adminOnlyNotifications = false;

    //Glow Entity Colors
    public Scoreboard scoreboard;
    public Team team;
    List<String> asTeams = new ArrayList<>();
    String lockedTeam = "ASLocked";
    String inUseTeam = "AS-InUse";

    //Blocked Names
    boolean enableBlockedNames = false;
    List<String> blockedNames = new ArrayList<>();

    //Debugging Options.... Not Exposed globally
    boolean debugFlag;

    private Scheduler scheduler;

    public ArmorStandEditorPlugin() {
        instance = this;
    }

    @Override
    public void onEnable() {
        scheduler = new Scheduler(this);

        if (!getHasFolia())
            scoreboard = Objects.requireNonNull(this.getServer().getScoreboardManager()).getMainScoreboard();

        //START ---  Load Messages in Console
        getLogger().info("======= ArmorStandEditor =======");
        getLogger().info("Plugin Version: v" + ASE_VERSION);

        hasPaper = getHasPaper();
        hasFolia = getHasFolia();

        //Get NMS Version
        nmsVersion = getServer().getMinecraftVersion();
        versionLogPrefix = warningMCVer + nmsVersion;
        doVersionCheck();

        //If Paper and Folia are both FALSE - Disable the plugin
        if (!hasPaper && !hasFolia) {
            getLogger().severe("This plugin requires either Paper or one of its forks to run. This is not an error, please do not report this!");
            getLogger().info(SEPARATOR_FIELD);
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().log(Level.INFO, "Paper/Folia Present? {0}", hasPaper);
        }

        if (!hasFolia) {
            scoreboard = Objects.requireNonNull(this.getServer().getScoreboardManager()).getMainScoreboard();
            registerScoreboards(scoreboard);
            if (!asTeams.contains(lockedTeam)) asTeams.add(lockedTeam);
            if (!asTeams.contains(inUseTeam)) asTeams.add(inUseTeam);
        } else {
            runWarningsFolia();
        }

        //Run the update checker if enabled in config
        if (getRunTheUpdateChecker()) {
            new UpdateChecker(this).checkForUpdates();
        }

        getLogger().info(SEPARATOR_FIELD);
        // ----- End of Initial Console Output

        //saveResource doesn't accept File.separator on Windows, need to hardcode unix separator "/" instead
        updateConfig("", "config.yml");
        updateConfig(languageFolderLocation, "de_DE.yml");
        updateConfig(languageFolderLocation, "es_ES.yml");
        updateConfig(languageFolderLocation, "fr_FR.yml");
        updateConfig(languageFolderLocation, "ja_JP.yml");
        updateConfig(languageFolderLocation, "nl_NL.yml");
        updateConfig(languageFolderLocation, "pl_PL.yml");
        updateConfig(languageFolderLocation, "pt_BR.yml");
        updateConfig(languageFolderLocation, "ro_RO.yml");
        updateConfig(languageFolderLocation, "ru_RU.yml");
        updateConfig(languageFolderLocation, "test_NA.yml");
        updateConfig(languageFolderLocation, "uk_UA.yml");
        updateConfig(languageFolderLocation, "zh_CN.yml");

        //English is the default language and needs to be unaltered to so that there is always a backup message string
        saveResource("lang/en_US.yml", true);

        loadConfigValues();

        //Get Metrics from bStats
        getMetrics();

        //Activate CoreProtect Extension if CoreProtect is present
        coreProtectExtension = new CoreProtectExtension(this);

        //Register Commands and Tab Completers
        CommandEx execute = new CommandEx(this);
        TabCompleter tabCompleter = new TabCompleter();

        //Register the same Command Executor and Tab Completer for all 3 commands - /ase, /armorstandeditor, and /asedit
        Objects.requireNonNull(getCommand("ase")).setExecutor(execute);
        Objects.requireNonNull(getCommand("armorstandeditor")).setExecutor(execute);
        Objects.requireNonNull(getCommand("asedit")).setExecutor(execute);

        Objects.requireNonNull(getCommand("ase")).setTabCompleter(tabCompleter);
        Objects.requireNonNull(getCommand("armorstandeditor")).setTabCompleter(tabCompleter);
        Objects.requireNonNull(getCommand("asedit")).setTabCompleter(tabCompleter);

        //Register Events
        editorManager = new PlayerEditorManager(this);
        getServer().getPluginManager().registerEvents(editorManager, this);
    }

    private void doVersionCheck() {
        if (VersionUtil.fromString(nmsVersion).isOlderThan(MinecraftVersion.OLDEST_SUPPORTED_VERSION)) {
            getLogger().severe(versionLogPrefix);
            getLogger().severe("ArmorStandEditor is not compatible with this version of Minecraft. Please update to at least version 1.17. Loading failed.");
            getLogger().info(SEPARATOR_FIELD);
            getServer().getPluginManager().disablePlugin(this);
        } else if (VersionUtil.fromString(nmsVersion).isOlderThanOrEquals(MinecraftVersion.MINECRAFT_26_1_1)) {
            getLogger().warning(versionLogPrefix);
            getLogger().warning("ArmorStandEditor is compatible with this version of Minecraft, but it is not the latest supported version.");
            getLogger().warning("Loading continuing, but please consider updating to the latest version.");
        } else {
            getLogger().info(versionLogPrefix);
            getLogger().info("ArmorStandEditor is compatible with this version of Minecraft. Loading continuing.");
        }
    }

    //Implement Glow Effects for Wolfstorm/ArmorStandEditor-Issues#5 - Add Disable Slots with Different Glow than Default
    private void registerScoreboards(Scoreboard scoreboard) {
        getLogger().info("Registering Scoreboards required for Glowing Effects");

        if (scoreboard.getTeam(inUseTeam) == null) {
            scoreboard.registerNewTeam(inUseTeam);
        } else {
            getLogger().info("Scoreboard for AS-InUse Already exists. Continuing to load");
        }

        if (scoreboard.getTeam(lockedTeam) == null) {
            scoreboard.registerNewTeam(lockedTeam);
            scoreboard.getTeam(lockedTeam).color(RED);
        } else {
            getLogger().info("Scoreboard for ASLocked Already exists. Continuing to load");
        }
    }

    private void unregisterScoreboards(Scoreboard scoreboard) {
        getLogger().info("Removing Scoreboards required for Glowing Effects when Disabling Slots...");

        team = scoreboard.getTeam(lockedTeam);
        if (team != null) {
            team.unregister();
        } else {
            getLogger().severe("Team Already Appears to be removed. Please do not do this manually!");
        }

        team = scoreboard.getTeam(inUseTeam);
        if (team != null) {
            team.unregister();
        } else {
            getLogger().severe("Team Already Appears to be removed. Please do not do this manually!");
        }
    }

    private void updateConfig(String folder, String config) {
        if (!new File(getDataFolder() + File.separator + folder + config).exists()) {
            saveResource(folder + config, false);
        }
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (PaperLib.getHolder(player.getOpenInventory().getTopInventory(), false).getHolder() == editorManager.getMenuHolder()) {
                player.closeInventory(InventoryCloseEvent.Reason.DISCONNECT);
            }
        }

        if (!hasFolia) {
            scoreboard = Objects.requireNonNull(this.getServer().getScoreboardManager()).getMainScoreboard();
            unregisterScoreboards(scoreboard);
        }
    }

    public String getNmsVersion() {
        return getServer().getMinecraftVersion();
    }

    public boolean getHasPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.Configuration");
            nmsVersionNotLatest = "PaperMC ASAP.";
            return true;
        } catch (ClassNotFoundException _) {
            nmsVersionNotLatest = "";
            return false;
        }
    }

    public boolean getHasFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
            return true;
        } catch (ClassNotFoundException _) {
            return false;
        }
    }

    public String getMinecraftVersion() {
        return this.nmsVersion;
    }

    // FIX: all getters now return cached fields instead of re-reading config on every call
    public boolean getArmorStandVisibility() { return armorStandVisibility; }

    public boolean getItemFrameVisibility() { return invisibleItemFrames; }

    public Language getLang() { return lang; }

    public double getMaxResetRange() { return maxResetRange; }

    public Material getEditTool() { return this.editTool; }

    public boolean getRunTheUpdateChecker() { return runTheUpdateChecker; }

    public boolean getDefaultGravity() { return defaultGravity; }

    // FIX: renamed from getallowedToRetrieveOwnPlayerHead (lowercase 'a' violated naming convention)
    public boolean getAllowedToRetrieveOwnPlayerHead() { return allowedToRetrieveOwnPlayerHead; }

    public boolean getAdminOnlyNotifications() { return adminOnlyNotifications; }

    public double getMinScaleValue() { return minScaleValue; }

    public double getMaxScaleValue() { return maxScaleValue; }

    // FIX: renamed to match field rename from customModelDataInt -> customModelDataValue
    public int getCustomModelDataValue() { return customModelDataValue; }

    public boolean isEditTool(ItemStack itemStk) {
        if (itemStk == null || editTool != itemStk.getType()) return false;

        ItemMeta itemMeta = itemStk.getItemMeta();
        if (itemMeta == null) return false;

        if (requireToolData && !hasMatchingDurability(itemMeta)) return false;
        if (requireToolName && !hasMatchingName(itemMeta)) return false;
        if (requireToolLore && !hasMatchingLore(itemMeta)) return false;
        if (allowCustomModelData && !hasMatchingCustomModelData(itemMeta)) return false;

        return true;
    }

    private boolean hasMatchingDurability(ItemMeta itemMeta) {
        Damageable damageable = (Damageable) itemMeta;
        return damageable.getDamage() == (short) editToolData;
    }

    private boolean hasMatchingName(ItemMeta itemMeta) {
        if (editToolName == null) return true;
        Component itemName = itemMeta.displayName();
        return itemName != null && itemName.equals(editToolName);
    }

    private boolean hasMatchingLore(ItemMeta itemMeta) {
        if (editToolLore == null) return true;
        List<Component> itemLore = itemMeta.lore();
        return itemLore != null && itemLore.equals((List<Component>) editToolLore);
    }

    private boolean hasMatchingCustomModelData(ItemMeta itemMeta) {
        if (customModelDataValue == 0) return true;
        CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
        if (component.getFloats().isEmpty()) return true;
        // FIX: was comparing float to float with ==; now both sides are cast to int for reliable equality
        return component.getFloats().getFirst().intValue() == customModelDataValue;
    }

    public void loadConfigValues() {
        lang = new Language(getConfig().getString("lang"), this);

        coarseRot = getConfig().getDouble("coarse");
        fineRot = getConfig().getDouble("fine");
        maxScaleValue = getConfig().getDouble("maxScaleValue");
        minScaleValue = getConfig().getDouble("minScaleValue");
        maxResetRange = getConfig().getDouble("maxResetRange");
        defaultGravity = getConfig().getBoolean("defaultGravitySetting", true);
        requireSneaking = getConfig().getBoolean("requireSneaking", false);
        sendToActionBar = getConfig().getBoolean("sendMessagesToActionBar", true);
        glowItemFrames = getConfig().getBoolean("glowingItemFrame", true);
        invisibleItemFrames = getConfig().getBoolean("invisibleItemFrames", true);
        runTheUpdateChecker = getConfig().getBoolean("runTheUpdateChecker", true);
        opUpdateNotification = getConfig().getBoolean("opUpdateNotification", true);
        updateCheckerInterval = getConfig().getDouble("updateCheckerInterval", 24);
        allowedToRetrieveOwnPlayerHead = getConfig().getBoolean("allowedToRetrieveOwnPlayerHead", true);
        adminOnlyNotifications = getConfig().getBoolean("adminOnlyNotifications", true);
        armorStandVisibility = getConfig().getBoolean("armorStandVisibility", true);
        requireToolData = getConfig().getBoolean("requireToolData", false);
        requireToolLore = getConfig().getBoolean("requireToolLore", false);
        requireToolName = getConfig().getBoolean("requireToolName", false);
        allowCustomModelData = getConfig().getBoolean("allowCustomModelData", false);

        loadTool();
        loadConditionalConfig();
    }

    private void loadTool() {
        toolType = getConfig().getString("tool");
        if (toolType == null) {
            getLogger().severe("Unable to get Tool for Use with Plugin. Unable to continue!");
            getLogger().info(SEPARATOR_FIELD);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        editTool = Material.getMaterial(toolType);
    }

    private void loadConditionalConfig() {
        if (allowCustomModelData) {
            // FIX: field renamed to customModelDataValue and typed as int
            customModelDataValue = getConfig().getInt("customModelDataInt", Integer.MIN_VALUE);
        }

        if (requireToolName) {
            editToolNameRaw = getConfig().getString("toolName", null);
            if (editToolNameRaw != null) {
                editToolName = LegacyComponentSerializer.legacyAmpersand().deserialize(editToolNameRaw);
            }
        }

        if (requireToolData) {
            editToolData = getConfig().getInt("toolData", Integer.MIN_VALUE);
        }

        if (requireToolLore) {
            editToolLore = getConfig().getList("toolLore", null);
        }

        if (enablePerWorld = getConfig().getBoolean("enablePerWorldSupport", false)) {
            allowedWorldList = getConfig().getList("allowed-worlds", null);
            if (allowedWorldList != null && !allowedWorldList.isEmpty() && allowedWorldList.getFirst().equals("*")) {
                allowedWorldList = getServer().getWorlds().stream().map(World::getName).toList();
            }
        }

        enableBlockedNames = getConfig().getBoolean("enableBlockedNames", true);
        if (enableBlockedNames) {
            blockedNames = getConfig().getStringList("blocked-names");
            if (!blockedNames.isEmpty()) {
                getLogger().info("Blocked Names Enabled. The following names are blocked from being used on Armor Stands:");
                blockedNames.forEach(name -> getLogger().info("- " + name));
            }
        }

        debugFlag = getConfig().getBoolean("debugFlag", false);
        if (debugFlag) {
            getServer().getLogger().log(Level.INFO, "[ArmorStandEditor-Debug] Debug Mode ENABLED! Use for testing only.");
        }
    }

    public void performReload() {
        // FIX: was fetching scoreboard twice and adding teams without duplicate checks
        if (!hasFolia) {
            scoreboard = Objects.requireNonNull(this.getServer().getScoreboardManager()).getMainScoreboard();
            unregisterScoreboards(scoreboard);
            registerScoreboards(scoreboard);
            if (!asTeams.contains(lockedTeam)) asTeams.add(lockedTeam);
            if (!asTeams.contains(inUseTeam)) asTeams.add(inUseTeam);
        } else {
            runWarningsFolia();
        }

        reloadConfig();
        loadConfigValues();
    }

    public static ArmorStandEditorPlugin instance() {
        return instance;
    }

    private void getMetrics() {
        Metrics metrics = new Metrics(this, PLUGIN_ID);

        metrics.addCustomChart(new SimplePie("tool_lore_enabled", () -> getConfig().getString("requireToolLore")));
        metrics.addCustomChart(new SimplePie("tool_data_enabled", () -> getConfig().getString("requireToolData")));
        metrics.addCustomChart(new SimplePie("action_bar_messages", () -> getConfig().getString("sendMessagesToActionBar")));
        metrics.addCustomChart(new SimplePie("require_sneaking", () -> getConfig().getString("requireSneaking")));

        metrics.addCustomChart(new DrilldownPie("language_used", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();

            // FIX: getString("lang") could return null, causing NPE on startsWith(); defaulting to "en"
            String languageUsed = getConfig().getString("lang", "en");
            if (languageUsed.startsWith("nl")) {
                map.put("Dutch", entry);
            } else if (languageUsed.startsWith("de")) {
                map.put("German", entry);
            } else if (languageUsed.startsWith("es")) {
                map.put("Spanish", entry);
            } else if (languageUsed.startsWith("fr")) {
                map.put("French", entry);
            } else if (languageUsed.startsWith("ja")) {
                map.put("Japanese", entry);
            } else if (languageUsed.startsWith("pl")) {
                map.put("Polish", entry);
            } else if (languageUsed.startsWith("ru")) {
                map.put("Russian", entry);
            } else if (languageUsed.startsWith("ro")) {
                map.put("Romanian", entry);
            } else if (languageUsed.startsWith("uk")) {
                map.put("Ukrainian", entry);
            } else if (languageUsed.startsWith("zh")) {
                map.put("Chinese", entry);
            } else if (languageUsed.startsWith("pt")) {
                map.put("Brazilian", entry);
            } else {
                map.put("English", entry);
            }
            return map;
        }));

        metrics.addCustomChart(new SimplePie("armor_stand_invisibility_usage", () -> getConfig().getString("armorStandVisibility")));
        metrics.addCustomChart(new SimplePie("itemframe_invisibility_used", () -> getConfig().getString("invisibleItemFrames")));
        metrics.addCustomChart(new SimplePie("custom_toolname_enabled", () -> getConfig().getString("requireToolName")));
        metrics.addCustomChart(new SimplePie("using_the_update_checker", () -> getConfig().getString("runTheUpdateChecker")));
        metrics.addCustomChart(new SimplePie("op_updates", () -> getConfig().getString("opUpdateNotification")));
        metrics.addCustomChart(new SimplePie("per_world_enabled", () -> String.valueOf(getConfig().getBoolean("enablePerWorldSupport"))));
        metrics.addCustomChart(new SimplePie("allowCustomModelData", () -> String.valueOf(getConfig().getBoolean("allowCustomModelData"))));
    }

    private void runWarningsFolia() {
        getLogger().warning("Scoreboards currently do not work on Folia. Scoreboard Coloring will not work");
    }

    public NamespacedKey getIconKey() {
        if (iconKey == null) iconKey = new NamespacedKey(this, "command_icon");
        return iconKey;
    }

    public boolean isDebug() {
        return debugFlag;
    }

    public String getASEVersion() {
        return ASE_VERSION;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public CoreProtectExtension getCoreProtectExtension() {
        return coreProtectExtension;
    }

}
