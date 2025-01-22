package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.devtools.Debug;

import io.github.rypofalem.armorstandeditor.language.Lang;
import io.github.rypofalem.armorstandeditor.metricshandler.MetricsHandler;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public final class ArmorStandEditorPlugin extends JavaPlugin {

    public static final String SEPARATOR_FIELD = "================================";

    // Classes
    private static ArmorStandEditorPlugin instance;
    private NamespacedKey iconKey;
    private Debug debug = new Debug(this);
    MetricsHandler metricsHandler;
    protected PlayerEditorManager editorManager;
    private Lang language;

    // Server Software Check True/False
    boolean isFolia;
    boolean isPaper;

    //Config Items (in Order)
    boolean debugFlag;  // Debug Flag - Not for Production
    boolean requireToolName;
    boolean requireToolData;
    boolean requireToolLore;
    boolean perWorldSupport;
    boolean requireSneaking;
    boolean sendToActionBar;
    boolean invisibleItemFrames;
    boolean invisibleArmorStands;

    String aseVersion;
    String nmsVersion;
    String editToolType;
    String editToolName;
    String editToolLore;

    double coarseRotation;
    double fineRotation;
    double minScaleValue;
    double maxScaleValue;

    Material editTool;
    Integer editToolData;
    List<?> allowedWorldList = null;

    // Format Strings
    String warningMCVer;
    String aseVersionToLog;
    String warningTeamAlreadyRegistered;
    String warningTeamAlreadyRemoved;
    String errorToolNameRequiredButNoName;

    // Teams
    Scoreboard scoreboard;
    Team team;
    List<String> asTeams = new ArrayList<>();
    String lockedTeam = "ASLocked";
    String inUseTeam = "AS-InUse";

    public ArmorStandEditorPlugin() {
        instance = this;
    }


    @Override
    public void onEnable() {
        // Plugin startup logic
        debugFlag = isDebug();

        if(debugFlag) {
            debug.log("Debug Mode = ON. SET FOR REWRITE");
        }

        debug.log("Enabling ASE, Getting ASE Version and logging Server Checks");

        aseVersion = getASEVersion();
        aseVersionToLog = String.format("Plugin Version: v%s", aseVersion);
        getLogger().log(Level.INFO,"======= ArmorStandEditor =======");
        getLogger().log(Level.INFO, aseVersionToLog );

        isFolia = isFolia();
        debug.log("Using Folia: " + isFolia);
        isPaper = isPaper();
        debug.log("Using Paper or a Fork: " + isPaper);

        if(isFolia || isPaper){
            getLogger().log(Level.INFO, "Server Type: Paper/Folia/Purpur or a Fork");
            getServer().getPluginManager().enablePlugin(this);
        } else {
            debug.log("Using SpigotMc: True, which no longer is supported");
            getLogger().log(Level.INFO, "Server Type: SpigotMC");
            getLogger().log(Level.SEVERE, "This Plugin No Longer works on Spigot. Disabling ArmorStandEditor");
            getServer().getPluginManager().disablePlugin(this);
        }

        // NMS Version Checks - Paper Versions
        nmsVersion = getServer().getMinecraftVersion();
        debug.log("Minecraft Server Version: " + nmsVersion);
        doNMSChecks(nmsVersion);

        // Setup the Teams and Scoreboards that ASE uses
        doScoreboardSetup();

        //TODO: Readd Languages here!
        language = new Lang(getLanguageConfig(), this);

        // Get all the Configuration Options
        coarseRotation       = getCoarseConfig();
        fineRotation         = getFineConfig();
        minScaleValue        = getMinScaleConfig();
        maxScaleValue        = getMaxScaleConfig();
        requireSneaking      = getRequireSneakingConfig();
        sendToActionBar      = getSendToActionBarConfig();
        invisibleArmorStands = getInvisibleArmorStandsConfig();
        invisibleItemFrames  = getInvisibleItemFramesConfig();

        // Everything requiring setup and (possible) Component Interactions
        editToolType    = getToolTypeConfig();
        doEditToolSetup(editToolType);

        requireToolName = getRequireToolNameConfig();
        doRequireToolNameSetup(requireToolName); // First Case of Conversion of &C to LegacyComponent

        requireToolData = getRequireToolDataConfig();
        doRequireToolDataSetup(requireToolData);

        requireToolLore = getRequireToolLoreConfig();
        doToolLoreSetup(requireToolLore);

        perWorldSupport = getPerWorldSupportConfig();
        doPerWorldSupportSetup(perWorldSupport);

        getLogger().log(Level.INFO, SEPARATOR_FIELD);

        // Do all the Metrics for BStats
        metricsHandler = new MetricsHandler(this);
        editorManager  = new PlayerEditorManager(this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        doScoreboardDestruction();
    }

    // Static Methods - Only Ran ON ENABLE / On Disable
    public static boolean isFolia(){
        try{
            Class.forName("io.papermc.paper.threadedregions.RegionziedServer");
            return true;
        } catch (ClassNotFoundException e){
            return false;
        }
    }
    public static boolean isPaper() {
        try{
            Class.forName("io.papermc.paper.text.PaperComponents");
            return true;
        } catch (ClassNotFoundException e){
            return false;
        }
    }
    private void doNMSChecks(String nmsVersion) {
        // Check if the Minecraft version is supported
        warningMCVer = String.format("Mineacraft Version: %s", nmsVersion);

        if (nmsVersion.contains("1.21")) {
            getLogger().log(Level.INFO, warningMCVer);
            getLogger().info("ArmorStandEditor is compatible with this version of Minecraft. Loading continuing.");
        } else if (nmsVersion.contains("1.17") || nmsVersion.contains("1.18") || nmsVersion.contains("1.19") || nmsVersion.contains("1.20")) {
            getLogger().log(Level.WARNING, warningMCVer);
            getLogger().warning("ArmorStandEditor is compatible with this version of Minecraft, but it is not the latest supported version.");
            getLogger().warning("Loading continuing, but please consider updating to the latest version.");
        } else {
            getLogger().log(Level.WARNING, warningMCVer);
            getLogger().warning("ArmorStandEditor is not compatible with this version of Minecraft. Please update to at least version 1.17. Loading failed.");
            getServer().getPluginManager().disablePlugin(this);
            getLogger().info(SEPARATOR_FIELD);
        }
    }

    // Setup and Destruction Methods
    private void doScoreboardSetup() {
        if(!isFolia){
            scoreboard = getServer().getScoreboardManager().getMainScoreboard();
            asTeams.add(lockedTeam);
            asTeams.add(inUseTeam);
            for (String teamToBeRegistered : asTeams) {
                scoreboard.registerNewTeam(teamToBeRegistered);
                team = scoreboard.getTeam(teamToBeRegistered);
                if (team != null) {
                    if (teamToBeRegistered.equals(lockedTeam)) {
                        getServer().getLogger().info("Registering Scoreboards required for Glowing Effects when Disabling Slots...");
                        scoreboard.getTeam(teamToBeRegistered).color(NamedTextColor.RED);
                    }
                } else {
                    warningTeamAlreadyRegistered = String.format("Scoreboard for Team '%s' already exists. Continuing to load", teamToBeRegistered);
                    getServer().getLogger().info(warningTeamAlreadyRegistered);
                }
            }
        }
    }
    private void doScoreboardDestruction() {
        if(!isFolia){
            getLogger().info("Removing Scoreboards required for Glowing Effects when Disabling Slots...");
            for (String teamToBeRegistered : asTeams) {
                team = scoreboard.getTeam(teamToBeRegistered);
                if (team != null) {
                    team.unregister();
                } else {
                    warningTeamAlreadyRemoved = String.format("Team '%s' already appears to be removed. Avoid manual removal to prevent errors!", teamToBeRegistered);
                    getServer().getLogger().severe(warningTeamAlreadyRemoved);
                }
            }
        }
    }
    private void doEditToolSetup(String editToolType){
        if(editToolType != null){
            editTool = Material.getMaterial(editToolType);
        } else{
            getLogger().severe("Unable to get Tool for Use with Plugin. Unable to continue!");
            getLogger().info(SEPARATOR_FIELD);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    private void doRequireToolNameSetup(boolean requireToolName){
        if(requireToolName){
            editToolName = getToolNameConfig();
            if(editToolName != null){
                editToolName = String.valueOf(LegacyComponentSerializer.legacyAmpersand().deserialize(editToolName));
            } else {
                errorToolNameRequiredButNoName = String.format("RequireToolName is set %b, but editToolName is empty. Please give your tool a name. Unable to Continue", requireToolName);
                getLogger().severe(errorToolNameRequiredButNoName);
                getLogger().info(SEPARATOR_FIELD);
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }
    private void doRequireToolDataSetup(boolean requireToolData){
        if(requireToolData){
            editToolData = getEditToolDataConfig();
        }
    }
    private void doToolLoreSetup(boolean requireToolLore){
        if(requireToolLore){
            editToolLore = getToolLoreConfig();
        }
    }
    private void doPerWorldSupportSetup(boolean perWorldSupport){
        if(perWorldSupport){
            allowedWorldList = getAllowedWorldListConfig();
            if(allowedWorldList != null && allowedWorldList.get(0).equals("*")){
                allowedWorldList = getServer().getWorlds().stream().map(World::getName).toList();
            }
        }
    }

    // Config Methods as Getters - Useful for Config Reload etc.
    public boolean isDebug() { return getConfig().getBoolean("debugFlag"); }
    public String getASEVersion() {
        return getConfig().getString("version");
    }
    public boolean getRequireToolNameConfig() { return getConfig().getBoolean("requireToolName", false); }
    public boolean getRequireToolDataConfig() { return getConfig().getBoolean("requireToolData", false); }
    public boolean getRequireToolLoreConfig() { return getConfig().getBoolean("requireToolLore", false); }
    public boolean getPerWorldSupportConfig() { return getConfig().getBoolean("enablePerWorldSupport", false); }
    public boolean getRequireSneakingConfig() { return getConfig().getBoolean("requireSneaking", false); }
    public boolean getInvisibleArmorStandsConfig() { return getConfig().getBoolean("armorStandVisibility", false); }
    public boolean getInvisibleItemFramesConfig() { return getConfig().getBoolean("invisibleItemFrames", false); }
    public boolean getSendToActionBarConfig() { return getConfig().getBoolean("sendMessagesToActionBar", false); }
    public double getCoarseConfig() { return getConfig().getDouble("coarse"); }
    public double getFineConfig() { return getConfig().getDouble("fine"); }
    public double getMinScaleConfig() { return getConfig().getDouble("minScaleValue"); }
    public double getMaxScaleConfig() { return getConfig().getDouble("maxScaleValue"); }
    public String getToolTypeConfig() { return getConfig().getString("tool");}
    public String getToolNameConfig() { return getConfig().getString("editToolName");}
    public String getToolLoreConfig(){ return getConfig().getString("toolLore", null); }
    public String getLanguageConfig(){ return getConfig().getString("lang"); }
    public Integer getEditToolDataConfig() { return getConfig().getInt("toolData", Integer.MIN_VALUE); }
    public List<?> getAllowedWorldListConfig(){ return getConfig().getList("allowed-worlds", null); }
    public Lang getLanguage(){
        return language;
    }

    public NamespacedKey getIconKey() {
        if (iconKey == null) iconKey = new NamespacedKey(this, "command_icon");
        return iconKey;
    }


}

