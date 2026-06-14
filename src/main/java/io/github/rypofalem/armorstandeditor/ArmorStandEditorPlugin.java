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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.papermc.lib.PaperLib;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.format.NamedTextColor.RED;


public class ArmorStandEditorPlugin extends JavaPlugin {

    //!!! DO NOT REMOVE THESE UNDER ANY CIRCUMSTANCES - Required for BStats and UpdateChecker !!!
    private static final int PLUGIN_ID = 12668;             //Used for BStats Metrics
    public Debug debug = new Debug(this);

    private final boolean unitTestMode;
    private boolean unitTestModeLogged;

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
    public static final String ASE_VERSION = "26.1.2-51.1";
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
    double maxNumberOfHeadRetrievals = 5;

    //Custom Data Model Support - Readded
    boolean allowCustomModelData = false;
    // FIX: renamed from customModelDataInt and retyped to int — it was float but used as an integer throughout
    double  customModelDataValue;

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

    private HeadDataManager headDataManager;

    public ArmorStandEditorPlugin() {
        instance = this;
        unitTestMode = false;
    }

    public ArmorStandEditorPlugin(Boolean utFlag){
        instance = this;
        unitTestMode = utFlag != null && utFlag;
    }

    @Override
    public void onEnable() {
        scheduler = new Scheduler(this);

        if (!getHasFolia())
            scoreboard = Objects.requireNonNull(this.getServer().getScoreboardManager()).getMainScoreboard();

        //START ---  Load Messages in Console
        getLogger().info("======= ArmorStandEditor =======");
        getLogger().info("Plugin Version: v" + ASE_VERSION);

        if(unitTestMode){
            getLogger().info("Unit Test Mode Enabled - Some features may be disabled or behave differently for testing purposes.");
            unitTestModeLogged = true;
        }

        hasPaper = getHasPaper();
        hasFolia = getHasFolia();

        //Get NMS Version
        nmsVersion = getServer().getMinecraftVersion();
        versionLogPrefix = warningMCVer + nmsVersion;
        doVersionCheck();

        //If Paper and Folia are both FALSE - Disable the plugin
        //Also we shouldn't run this if we are UnitTesting since we don't care.
        if(unitTestMode){
            getLogger().info("Skipping Paper and Folia check due to Unit Test Mode.");
        } else if (!hasPaper && !hasFolia) {
            getLogger().severe("This plugin requires either Paper or one of its forks to run. This is not an error, please do not report this!");
            getLogger().info(SEPARATOR_FIELD);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }  else {
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

        //Needed to handle the persistent storage of the PlayerHead Counters
        headDataManager = new HeadDataManager(this);

        //Get Metrics from bStats
        getMetrics();

        //Activate CoreProtect Extension if CoreProtect is present
        coreProtectExtension = new CoreProtectExtension(this);

        //Register Commands and Tab Completers
        CommandEx execute = new CommandEx(this);
        TabCompleter tabCompleter = new TabCompleter();

        // Register commands using the Paper Lifecycle API (replaces getCommand() which is
        // unsupported for Paper plugins). Wraps the existing CommandEx and TabCompleter so
        // no other code needs to change.
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            for (String alias : List.of("ase", "armorstandeditor", "asedit")) {
                // Stub Command so TabCompleter.onTabComplete() can call command.getName()
                // without a NullPointerException — Brigadier has no equivalent object to pass.
                org.bukkit.command.Command stubCommand = new org.bukkit.command.Command(alias) {
                    @Override
                    public boolean execute(CommandSender sender, String label, String[] args) {
                        return false;
                    }
                };
                commands.register(
                        Commands.literal(alias)
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();
                                    execute.onCommand(sender, stubCommand, alias, new String[0]);
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("args", StringArgumentType.greedyString())
                                                .suggests((ctx, builder) -> {
                                                    CommandSender sender = ctx.getSource().getSender();
                                                    String[] args = builder.getRemaining().split(" ", -1);
                                                    List<String> completions = tabCompleter.onTabComplete(sender, stubCommand, alias, args);
                                                    if (completions != null) completions.forEach(builder::suggest);
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    CommandSender sender = ctx.getSource().getSender();
                                                    String[] args = ctx.getArgument("args", String.class).split(" ", -1);
                                                    execute.onCommand(sender, stubCommand, alias, args);
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                                .build(),
                        "ArmorStandEditor command"
                );
            }
        });

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

    public double getMaxNumberOfHeadRetrievals() { return maxNumberOfHeadRetrievals; }

    public boolean getAdminOnlyNotifications() { return adminOnlyNotifications; }

    public double getMinScaleValue() { return minScaleValue; }

    public double getMaxScaleValue() { return maxScaleValue; }

    public boolean isEditTool(ItemStack itemStk) {
        if (itemStk == null || editTool != itemStk.getType()) return false;

        if (requireToolData || requireToolName || requireToolLore || allowCustomModelData) {
            ItemMeta itemMeta = itemStk.getItemMeta();
            if (itemMeta == null) return false;
            if (requireToolData && !hasMatchingDurability(itemMeta)) return false;
            if (requireToolName && !hasMatchingName(itemMeta)) return false;
            if (requireToolLore && !hasMatchingLore(itemMeta)) return false;
            if (allowCustomModelData && !hasMatchingCustomModelData(itemMeta)) return false;
        }

        return true;
    }

    private boolean hasMatchingDurability(ItemMeta itemMeta) {
        if (!(itemMeta instanceof Damageable damageable))  return false;
        return damageable.hasDamage() && damageable.getDamage() == editToolData;
    }

    private boolean hasMatchingName(ItemMeta itemMeta) {
        if (editToolName == null) return true;
        Component itemName = itemMeta.displayName();
        return itemName != null && itemName.equals(editToolName);
    }

    private boolean hasMatchingLore(ItemMeta itemMeta) {
        if (editToolLore == null) return true;
        List<Component> itemLore = itemMeta.lore();
        return itemLore != null && itemLore.equals(editToolLore);
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean hasMatchingCustomModelData(ItemMeta itemMeta) {
        if (customModelDataValue == 0.0) return true;
        if (!itemMeta.hasCustomModelDataComponent()) return false;
        return itemMeta.getCustomModelDataComponent().getFloats().get(0) == customModelDataValue;
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
        adminOnlyNotifications = getConfig().getBoolean("adminOnlyNotifications", true);
        armorStandVisibility = getConfig().getBoolean("armorStandVisibility", true);
        requireToolData = getConfig().getBoolean("requireToolData", false);
        requireToolLore = getConfig().getBoolean("requireToolLore", false);
        requireToolName = getConfig().getBoolean("requireToolName", false);

        //Conditional Config Items
        allowCustomModelData = getConfig().getBoolean("allowCustomModelData", false);
        enablePerWorld = getConfig().getBoolean("enablePerWorldSupport", false);
        allowedToRetrieveOwnPlayerHead = getConfig().getBoolean("allowedToRetrieveOwnPlayerHead", true);
        enableBlockedNames = getConfig().getBoolean("enableBlockedNames", true);
        debugFlag = getConfig().getBoolean("debugFlag", false);

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

    public void loadConditionalConfig() {
        // Reset all conditional fields first
        customModelDataValue = 0;
        editToolNameRaw = null;
        editToolName = null;
        editToolData = Integer.MIN_VALUE;
        editToolLore = null;
        allowedWorldList = null;
        blockedNames = List.of();

        if (allowCustomModelData) {
            customModelDataValue = getConfig().getDouble("customModelDataInt", 10.0f);
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
            editToolLore = getConfig().getList("toolLore", null).stream()
                    .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(String.valueOf(line))).toList();
        }

        if (enablePerWorld) {
            allowedWorldList = new ArrayList<>(getConfig().getStringList("allowed-worlds"));
            if (!allowedWorldList.isEmpty() && allowedWorldList.get(0).equals("*")) {
                allowedWorldList = getServer().getWorlds().stream()
                        .map(World::getName)
                        .toList();
            }
        }

        maxNumberOfHeadRetrievals = getConfig().getDouble("maxNumberOfHeadRetrievals", 5);

        if (enableBlockedNames) {
            blockedNames = List.copyOf(getConfig().getStringList("blocked-names"));
            if (!blockedNames.isEmpty()) {
                getLogger().info("Blocked Names Enabled. The following names are blocked:");
                blockedNames.forEach(name -> getLogger().info("- " + name));
            }
        }

        if (debugFlag) {
            getLogger().log(Level.INFO, "[ArmorStandEditor-Debug] Debug Mode ENABLED! Use for testing only.");
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

    public HeadDataManager getHeadDataMananger() {
        return headDataManager;
    }

    /*
    * HELPERS FOR UNIT TESTING
     */
    public boolean didLogUnitTestMode() {
        return unitTestModeLogged;
    }

    public List<String> getAllowedWorldList() {
        return (List<String>) allowedWorldList;
    }

    public boolean getEnableBlockedNames(){
        return getConfig().getBoolean("enableBlockedNames", true);
    }

    public List<String> getListOfBlockedNames() {
        return getConfig().getStringList("blocked-names");
    }

    public boolean getAllowCustomModelData() {
        return getConfig().getBoolean("allowCustomModelData", true);
    }

    public boolean getEnablePerWorldSupport() {
        return getConfig().getBoolean("enablePerWorldSupport", false);
    }

    public boolean getRequiredToolName() { return getConfig().getBoolean("requireToolName", false); }

    public boolean getRequiredToolLore(){ return getConfig().getBoolean("requireToolLore", false); }

    public boolean getRequiredToolData(){ return getConfig().getBoolean("requireToolData", false); }

    public double getCustomModelDataValue() {
        if (!getAllowCustomModelData()) {
            return 0.0;
        }
        return getConfig().getDouble("customModelDataInt", 0.0);
    }


}