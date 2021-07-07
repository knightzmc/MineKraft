package org.kryptonmc.krypton.util;

import org.kryptonmc.krypton.entity.KryptonEntity;
import org.kryptonmc.krypton.world.chunk.ChunkPosition;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3i;

public final class CoordinateUtils {

    private CoordinateUtils() {
        throw new UnsupportedOperationException();
    }

    // dx, dz are relative to the target chunk
    // dx, dz in [-radius, radius]
    public static int getNeighbourMappedIndex(final int dx, final int dz, final int radius) {
        return (dx + radius) + (2 * radius + 1)*(dz + radius);
    }

    // the chunk keys are compatible with vanilla

    public static long getChunkKey(final Vector3i pos) {
        return ((long)(pos.z() >> 4) << 32) | ((pos.x() >> 4) & 0xFFFFFFFFL);
    }

    public static long getChunkKey(final KryptonEntity entity) {
        return ((GenericMath.floorl(entity.getLocation().getZ()) >> 4) << 32) | ((GenericMath.floorl(entity.getLocation().getX()) >> 4) & 0xFFFFFFFFL);
    }

    public static long getChunkKey(final ChunkPosition pos) {
        return ((long)pos.getZ() << 32) | (pos.getX() & 0xFFFFFFFFL);
    }

    public static long getChunkKey(final int x, final int z) {
        return ((long)z << 32) | (x & 0xFFFFFFFFL);
    }

    public static int getChunkX(final long chunkKey) {
        return (int)chunkKey;
    }

    public static int getChunkZ(final long chunkKey) {
        return (int)(chunkKey >>> 32);
    }

    public static int getChunkCoordinate(final double blockCoordinate) {
        return GenericMath.floor(blockCoordinate) >> 4;
    }

    // the section keys are compatible with vanilla's

    static final int SECTION_X_BITS = 22;
    static final long SECTION_X_MASK = (1L << SECTION_X_BITS) - 1;
    static final int SECTION_Y_BITS = 20;
    static final long SECTION_Y_MASK = (1L << SECTION_Y_BITS) - 1;
    static final int SECTION_Z_BITS = 22;
    static final long SECTION_Z_MASK = (1L << SECTION_Z_BITS) - 1;
    // format is y,z,x (in order of LSB to MSB)
    static final int SECTION_Y_SHIFT = 0;
    static final int SECTION_Z_SHIFT = SECTION_Y_SHIFT + SECTION_Y_BITS;
    static final int SECTION_X_SHIFT = SECTION_Z_SHIFT + SECTION_X_BITS;
    static final int SECTION_TO_BLOCK_SHIFT = 4;

    public static long getChunkSectionKey(final int x, final int y, final int z) {
        return ((x & SECTION_X_MASK) << SECTION_X_SHIFT)
                | ((y & SECTION_Y_MASK) << SECTION_Y_SHIFT)
                | ((z & SECTION_Z_MASK) << SECTION_Z_SHIFT);
    }

    public static long getChunkSectionKey(final ChunkPosition pos, final int y) {
        return ((pos.getX() & SECTION_X_MASK) << SECTION_X_SHIFT)
                | ((y & SECTION_Y_MASK) << SECTION_Y_SHIFT)
                | ((pos.getZ() & SECTION_Z_MASK) << SECTION_Z_SHIFT);
    }

    public static long getChunkSectionKey(final Vector3i pos) {
        return (((long)pos.x() << (SECTION_X_SHIFT - SECTION_TO_BLOCK_SHIFT)) & (SECTION_X_MASK << SECTION_X_SHIFT)) |
                ((pos.y() >> SECTION_TO_BLOCK_SHIFT) & (SECTION_Y_MASK << SECTION_Y_SHIFT)) |
                (((long)pos.z() << (SECTION_Z_SHIFT - SECTION_TO_BLOCK_SHIFT)) & (SECTION_Z_MASK << SECTION_Z_SHIFT));
    }

    public static long getChunkSectionKey(final KryptonEntity entity) {
        return ((GenericMath.floorl(entity.getLocation().getX()) << (SECTION_X_SHIFT - SECTION_TO_BLOCK_SHIFT)) & (SECTION_X_MASK << SECTION_X_SHIFT)) |
                ((GenericMath.floorl(entity.getLocation().getY()) >> SECTION_TO_BLOCK_SHIFT) & (SECTION_Y_MASK << SECTION_Y_SHIFT)) |
                ((GenericMath.floorl(entity.getLocation().getZ()) << (SECTION_Z_SHIFT - SECTION_TO_BLOCK_SHIFT)) & (SECTION_Z_MASK << SECTION_Z_SHIFT));
    }

    public static int getChunkSectionX(final long key) {
        return (int)(key << (Long.SIZE - (SECTION_X_SHIFT + SECTION_X_BITS)) >> (Long.SIZE - SECTION_X_BITS));
    }

    public static int getChunkSectionY(final long key) {
        return (int)(key << (Long.SIZE - (SECTION_Y_SHIFT + SECTION_Y_BITS)) >> (Long.SIZE - SECTION_Y_BITS));
    }

    public static int getChunkSectionZ(final long key) {
        return (int)(key << (Long.SIZE - (SECTION_Z_SHIFT + SECTION_Z_BITS)) >> (Long.SIZE - SECTION_Z_BITS));
    }

    // the block coordinates are not necessarily compatible with vanilla's

    public static int getBlockCoordinate(final double blockCoordinate) {
        return GenericMath.floor(blockCoordinate);
    }

    public static long getBlockKey(final int x, final int y, final int z) {
        return ((long)x & 0x7FFFFFF) | (((long)z & 0x7FFFFFF) << 27) | ((long)y << 54);
    }

    public static long getBlockKey(final Vector3i pos) {
        return ((long)pos.x() & 0x7FFFFFF) | (((long)pos.z() & 0x7FFFFFF) << 27) | ((long)pos.y() << 54);
    }

    public static long getBlockKey(final KryptonEntity entity) {
        return ((long)entity.getLocation().getX() & 0x7FFFFFF) | (((long)entity.getLocation().getZ() & 0x7FFFFFF) << 27) | ((long)entity.getLocation().getY() << 54);
    }
}

