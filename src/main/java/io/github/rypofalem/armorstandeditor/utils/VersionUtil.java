package io.github.rypofalem.armorstandeditor.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionUtil  implements  Comparable<VersionUtil>{
/*
    private VersionUtil() {}

    /**
     * Returns true if current >= target (by Minecraft version).
     *
     * current can be:
     *  - "1.21.10", "1.21.4", "1.21.4-pre1", "1.21.4-SNAPSHOT" (Paper/Folia)
     *  - "v1_21_R3" (Spigot/Bukkit NMS package)
     *
     * target should be a normal MC version like "1.21.4".

    public static boolean isAtLeast(String current, String target) {
        String normCurrent = normalize(current);
        String normTarget  = normalize(target);

        int[] curParts = parseVersion(normCurrent);
        int[] tarParts = parseVersion(normTarget);

        int len = Math.max(curParts.length, tarParts.length);
        for (int i = 0; i < len; i++) {
            int c = i < curParts.length ? curParts[i] : 0;
            int t = i < tarParts.length ? tarParts[i] : 0;

            if (c != t) {
                return c > t;
            }
        }
        return true; // equal
    }

    /**
     * Normalizes:
     *  - Paper/Folia style "1.21.10", "1.21.10-pre4", "1.21.10-SNAPSHOT"
     *  - Spigot NMS style "v1_21_R3"
     * into a "major.minor.patch" string.

    private static String normalize(String version) {
        if (version == null) return "0.0.0";
        version = version.trim();

        // Case A: Spigot NMS "v1_21_RX"
        if (version.startsWith("v") && version.contains("_")) {
            // Strip leading 'v'
            String body = version.substring(1); // e.g. "1_21_R3"
            String[] parts = body.split("_");
            if (parts.length >= 3) {
                String majorStr = digitsOnly(parts[0]); // "1"
                String minorStr = digitsOnly(parts[1]); // "21"
                String rStr     = digitsOnly(parts[2]); // "3" for R3

                int major = parseIntOrZero(majorStr);
                int minor = parseIntOrZero(minorStr);
                int r     = parseIntOrZero(rStr);

                // Map 1_21_Rx -> minimum MCP version as per wiki
                //  R1 -> 1.21.0
                //  R2 -> 1.21.2
                //  R3 -> 1.21.4
                //  R4 -> 1.21.5
                //  R5 -> 1.21.6
                int patch;
                if (major == 1 && minor == 21) {
                    switch (r) {
                        case 1: patch = 0; break;
                        case 2: patch = 2; break;
                        case 3: patch = 4; break;
                        case 4: patch = 5; break;
                        case 5: patch = 6; break;
                        default:
                            // Future R versions for 1.21: assume >= 1.21.6
                            patch = 6;
                            break;
                    }
                } else {
                    // Fallback for other majors/minors if needed
                    patch = 0;
                }

                return major + "." + minor + "." + patch;
            }
        }

        // Case B: Paper/Folia "1.21.10", maybe with suffixes
        // Keep only the leading 0-9 and dots
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < version.length(); i++) {
            char c = version.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.') {
                sb.append(c);
            } else if (sb.length() > 0) {
                break;
            }
        }

        if (sb.length() == 0) {
            return "0.0.0";
        }

        String[] nums = sb.toString().split("\\.");
        if (nums.length == 1) {
            return nums[0] + ".0.0";
        } else if (nums.length == 2) {
            return nums[0] + "." + nums[1] + ".0";
        } else {
            return sb.toString();
        }
    }

    private static String digitsOnly(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }*/

    private final String version;
    private static final Pattern SEPARATOR = Pattern.compile("\\.");
    private static final Pattern VERSION_NUMBER = Pattern.compile("[0-9]+(" + SEPARATOR + "[0-9]+)*");
    private final String[] versionComponents;

    private VersionUtil(String version){
        this.version = version;
        versionComponents = version.split(SEPARATOR.pattern());
    }

    public static String fromString(String version){
        if (version == null)
            throw new IllegalArgumentException("Version can not be null");

        Matcher matcher = VERSION_NUMBER.matcher(version);

        if(!matcher.find())
            throw new IllegalArgumentException("Invalid Version Format: " + version);

        return new VersionUtil(matcher.group(0));
    }

    public int compareTo(@NotNull VersionUtil that){
        int length = Math.max(versionComponents.length, that.versionComponents.length);
        for(int i = 0; i < length; i++) {
            int thisPart = i < versionComponents.length ? Integer.parseInt(versionComponents[i]) : 0;
            int thatPart = i < that.versionComponents.length ? Integer.parseInt(that.versionComponents[i]) : 0;

            if (thisPart < thatPart) {
                return -1;
            }

            if (thisPart > thatPart) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionUtil)) return false;
        VersionUtil version1 = (VersionUtil) o;
        return Objects.equals(version, version1.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return version;
    }

    @NotNull
    public String[] getComponents() {
        return versionComponents;
    }

    public boolean isPreRelease() {
        try {
            return Integer.parseInt(this.versionComponents[versionComponents.length-1]) != 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isNewerThanOrEquals(@NotNull VersionUtil other) {
        return this.compareTo(other) >= 0;
    }
}
