package org.kryptonmc.krypton.util;

import org.kryptonmc.krypton.world.HeightAccessor;

public final class WorldUtil {

    private WorldUtil() {
        throw new UnsupportedOperationException();
    }

    public static int getMaxSection(final HeightAccessor world) {
        return world.getMaximumSection() - 1;
    }

    public static int getMinSection(final HeightAccessor world) {
        return world.getMinimumSection();
    }

    public static int getMaxLightSection(final HeightAccessor world) {
        return getMaxSection(world) + 1;
    }

    public static int getMinLightSection(final HeightAccessor world) {
        return getMinSection(world) - 1;
    }

    public static int getTotalSections(final HeightAccessor world) {
        return getMaxSection(world) - getMinSection(world) + 1;
    }

    public static int getTotalLightSections(final HeightAccessor world) {
        return getMaxLightSection(world) - getMinLightSection(world) + 1;
    }

    public static int getMinBlockY(final HeightAccessor world) {
        return getMinSection(world) << 4;
    }

    public static int getMaxBlockY(final HeightAccessor world) {
        return (getMaxSection(world) << 4) | 15;
    }
}
