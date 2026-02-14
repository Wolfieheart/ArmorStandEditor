package io.github.rypofalem.armorstandeditor.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtil implements Comparable<VersionUtil> {

    private final String version;
    private static final Pattern SEPARATOR = Pattern.compile("\\.");
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("\\d+(?:" + SEPARATOR + "\\d+)*");
    private final String[] components;

    private VersionUtil(String version) {
        this.version = version;
        components = version.split(SEPARATOR.pattern());
    }

    /**
     * Constructs a Version object from the given string.
     *
     * <p>This method will truncate any extraneous characters found
     * after it matches the first qualified version string.</p>
     *
     * @param version A string that contains a formatted version.
     * @return A new Version instance from the given string.
     */
    public static VersionUtil fromString(String version) {
        if (version == null) {
            throw new IllegalArgumentException("Version can not be null");
        }

        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        return new VersionUtil(matcher.group(0));
    }

    @Override
    public int compareTo(@NotNull VersionUtil that) {
        int length = Math.max(components.length, that.components.length);

        for (int i = 0; i < length; i++) {
            int thisPart = i < components.length ? Integer.parseInt(components[i]) : 0;
            int thatPart = i < that.components.length ? Integer.parseInt(that.components[i]) : 0;

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

    public boolean isNewerThanOrEquals(@NotNull VersionUtil other) {
        return this.compareTo(other) >= 0;
    }

    public boolean isOlderThanOrEquals(@NotNull VersionUtil other) {
        return this.compareTo(other) <= 0;
    }

    public boolean isOlderThan(@NotNull VersionUtil other) {
        return this.compareTo(other) < 0;
    }


}
