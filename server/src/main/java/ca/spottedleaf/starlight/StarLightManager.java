package ca.spottedleaf.starlight;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kryptonmc.krypton.util.CoordinateUtils;
import org.kryptonmc.krypton.util.WorldUtil;
import org.kryptonmc.krypton.world.KryptonWorld;
import org.kryptonmc.krypton.world.chunk.ChunkManager;
import org.kryptonmc.krypton.world.chunk.ChunkPosition;
import org.kryptonmc.krypton.world.chunk.KryptonChunk;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3i;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class StarLightManager {

    private static final int MAXIMUM_LIGHT_LEVEL = 15;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(GenericMath.clamp(Runtime.getRuntime().availableProcessors() - 2, 1, 8));

    protected final KryptonWorld world;
    protected final ChunkManager chunkManager;

    protected final ArrayDeque<SkyStarLightEngine> cachedSkyPropagators;
    protected final ArrayDeque<BlockStarLightEngine> cachedBlockPropagators;

    protected final LightQueue lightQueue = new LightQueue(this);
    private final AtomicBoolean scheduled = new AtomicBoolean();

    protected final int minSection;
    protected final int maxSection;
    protected final int minLightSection;
    protected final int maxLightSection;

    public StarLightManager(final @NotNull ChunkManager chunkManager, final boolean hasSkyLight) {
        this.chunkManager = chunkManager;
        this.world = chunkManager.getWorld();
        this.cachedSkyPropagators = hasSkyLight ? new ArrayDeque<>() : null;
        this.cachedBlockPropagators = new ArrayDeque<>();
        this.minSection = WorldUtil.getMinSection(world);
        this.maxSection = WorldUtil.getMaxSection(world);
        this.minLightSection = WorldUtil.getMinLightSection(world);
        this.maxLightSection = WorldUtil.getMaxLightSection(world);
    }

    public int getMinLightSection() {
        return minLightSection;
    }

    public int getMaxLightSection() {
        return maxLightSection;
    }

    public int getLightSectionCount() {
        return world.getSectionCount() + 2;
    }

    public @Nullable SWMRNibbleArray getSkyData(final int x, final int y, final int z) {
        final KryptonChunk chunk = chunkManager.get(x, z);
        if (chunk == null || !chunk.isLightOn()) return null;
        if (y > maxLightSection || y < minLightSection) return null;
        if (chunk.getSkyEmptinessMap() == null) return null;
        return chunk.getSkyNibbles()[y - minLightSection];
    }

    public int getSkyLightValue(final @NotNull Vector3i position) {
        final int x = position.x();
        int y = position.y();
        final int z = position.z();

        final KryptonChunk chunk = chunkManager.get(x >> 4, z >> 4);
        if (chunk == null) return MAXIMUM_LIGHT_LEVEL;

        int sectionY = y >> 4;
        if (sectionY > maxLightSection) return MAXIMUM_LIGHT_LEVEL;
        if (sectionY < minLightSection) {
            sectionY = minLightSection;
            y = sectionY << 4;
        }

        final SWMRNibbleArray[] nibbles = chunk.getSkyNibbles();
        final SWMRNibbleArray immediate = nibbles[sectionY - minLightSection];
        if (!immediate.isNullNibbleVisible()) return immediate.getVisible(x, y, z);

        final boolean[] emptinessMap = chunk.getSkyEmptinessMap();
        if (emptinessMap == null) return MAXIMUM_LIGHT_LEVEL;

        // are we above this chunk's lowest empty section?
        int lowestY = minLightSection - 1;
        for (int currY = maxSection; currY >= minSection; --currY) {
            if (emptinessMap[currY - minSection]) continue;
            lowestY = currY;
            break;
        }
        if (sectionY > lowestY) return MAXIMUM_LIGHT_LEVEL;

        // this nibble is going to depend solely on the skylight data above it
        // find first non-null data above (there does exist one, as we just found it above)
        for (int currY = sectionY + 1; currY <= maxLightSection; ++currY) {
            final SWMRNibbleArray nibble = nibbles[currY - minLightSection];
            if (!nibble.isNullNibbleVisible()) return nibble.getVisible(x, 0, z);
        }

        // should never reach here
        return MAXIMUM_LIGHT_LEVEL;
    }

    public @Nullable SWMRNibbleArray getBlockData(final int x, final int y, final int z) {
        final KryptonChunk chunk = chunkManager.get(x, z);
        if (chunk == null || y < minLightSection || y > maxLightSection) return null;
        return chunk.getBlockNibbles()[y - minLightSection];
    }

    public int getBlockLightValue(final @NotNull Vector3i position) {
        final int cx = position.x() >> 4;
        final int cy = position.y() >> 4;
        final int cz = position.z() >> 4;
        if (cy < minLightSection || cy > maxLightSection) return 0;

        final KryptonChunk chunk = chunkManager.get(cx, cz);
        if (chunk == null) return 0;

        final SWMRNibbleArray nibble = chunk.getBlockNibbles()[cy - minLightSection];
        return nibble.getVisible(position.x(), position.y(), position.z());
    }

    public boolean hasUpdates() {
        return !lightQueue.isEmpty();
    }

    public final @Nullable SkyStarLightEngine getSkyEngine() {
        if (cachedSkyPropagators == null) return null;
        final SkyStarLightEngine ret;
        synchronized (cachedSkyPropagators) {
            ret = cachedSkyPropagators.pollFirst();
        }
        if (ret == null) return new SkyStarLightEngine(world);
        return ret;
    }

    public final void releaseSkyEngine(final @NotNull SkyStarLightEngine engine) {
        if (cachedSkyPropagators == null) return;
        synchronized (cachedSkyPropagators) {
            cachedSkyPropagators.addFirst(engine);
        }
    }

    public final @NotNull BlockStarLightEngine getBlockEngine() {
        final BlockStarLightEngine ret;
        synchronized (cachedBlockPropagators) {
            ret = cachedBlockPropagators.pollFirst();
        }
        if (ret == null) return new BlockStarLightEngine(world);
        return ret;
    }

    public final void releaseBlockEngine(final @NotNull BlockStarLightEngine engine) {
        synchronized (cachedBlockPropagators) {
            cachedBlockPropagators.addFirst(engine);
        }
    }

    public @Nullable CompletableFuture<@NotNull Void> blockChange(final @NotNull Vector3i position) {
        if (position.y() < WorldUtil.getMinBlockY(world) || position.y() > WorldUtil.getMaxBlockY(world)) return null;
        return lightQueue.queueBlockChange(position);
    }

    public @NotNull CompletableFuture<@NotNull Void> sectionChange(final int x, final int y, final int z, final boolean newEmptyValue) {
        return lightQueue.queueSectionChange(x, y, z, newEmptyValue);
    }

    public void forceLoadInChunk(final KryptonChunk chunk, final Boolean[] emptySections) {
        final SkyStarLightEngine skyEngine = getSkyEngine();
        final BlockStarLightEngine blockEngine = getBlockEngine();

        try {
            if (skyEngine != null) skyEngine.forceHandleEmptySectionChanges(chunkManager, chunk, emptySections);
            blockEngine.forceHandleEmptySectionChanges(chunkManager, chunk, emptySections);
        } finally {
            if (skyEngine != null) releaseSkyEngine(skyEngine);
            releaseBlockEngine(blockEngine);
        }
    }

    public void lightChunk(final KryptonChunk chunk, final boolean isLit) {
        final ChunkPosition position = chunk.getPosition();
        final Runnable task = () -> {
            final Boolean[] emptySections = StarLightEngine.getEmptySectionsForChunk(chunk);
            if (!isLit) {
                chunk.setLightOn(false);
                lightChunk(chunk, emptySections);
                chunk.setLightOn(true);
            } else {
                forceLoadInChunk(chunk, emptySections);
                checkChunkEdges(position.getX(), position.getZ());
            }
        };
        scheduleChunkLight(position, task);
        scheduleUpdates();
    }

    public void lightChunk(final KryptonChunk chunk, final Boolean[] emptySections) {
        final SkyStarLightEngine skyEngine = this.getSkyEngine();
        final BlockStarLightEngine blockEngine = this.getBlockEngine();

        try {
            if (skyEngine != null) skyEngine.light(this.chunkManager, chunk, emptySections);
            blockEngine.light(this.chunkManager, chunk, emptySections);
        } finally {
            if (skyEngine != null) releaseSkyEngine(skyEngine);
            this.releaseBlockEngine(blockEngine);
        }
    }

    public void checkChunkEdges(final int chunkX, final int chunkZ) {
        checkSkyEdges(chunkX, chunkZ);
        checkBlockEdges(chunkX, chunkZ);
    }

    public void checkSkyEdges(final int chunkX, final int chunkZ) {
        final SkyStarLightEngine skyEngine = getSkyEngine();
        if (skyEngine == null) return;
        try {
            skyEngine.checkChunkEdges(chunkManager, chunkX, chunkZ);
        } finally {
            releaseSkyEngine(skyEngine);
        }
    }

    public void checkBlockEdges(final int chunkX, final int chunkZ) {
        final BlockStarLightEngine blockEngine = getBlockEngine();
        try {
            blockEngine.checkChunkEdges(chunkManager, chunkX, chunkZ);
        } finally {
            releaseBlockEngine(blockEngine);
        }
    }

    public void scheduleChunkLight(final ChunkPosition position, final Runnable task) {
        lightQueue.queueChunkLighting(position, task);
    }

    public void scheduleUpdates() {
        if (hasUpdates() && scheduled.compareAndSet(false, true)) {
            EXECUTOR.submit(() -> {
                propagateChanges();
                scheduled.set(false);
            });
        }
    }

    public void propagateChanges() {
        if (this.lightQueue.isEmpty()) {
            return;
        }

        final SkyStarLightEngine skyEngine = this.getSkyEngine();
        final BlockStarLightEngine blockEngine = this.getBlockEngine();

        try {
            LightQueue.ChunkTasks task;
            while ((task = this.lightQueue.removeFirstTask()) != null) {
                if (task.lightTasks != null) {
                    for (final Runnable run : task.lightTasks) {
                        run.run();
                    }
                }

                final long coordinate = task.chunkCoordinate;
                final int chunkX = CoordinateUtils.getChunkX(coordinate);
                final int chunkZ = CoordinateUtils.getChunkZ(coordinate);

                final Set<Vector3i> positions = task.changedPositions;
                final Boolean[] sectionChanges = task.changedSectionSet;

                if (skyEngine != null && (!positions.isEmpty() || sectionChanges != null)) {
                    skyEngine.blocksChangedInChunk(this.chunkManager, chunkX, chunkZ, positions, sectionChanges);
                }
                if (!positions.isEmpty() || sectionChanges != null) {
                    blockEngine.blocksChangedInChunk(this.chunkManager, chunkX, chunkZ, positions, sectionChanges);
                }

                if (skyEngine != null && task.queuedEdgeChecksSky != null) {
                    skyEngine.checkChunkEdges(this.chunkManager, chunkX, chunkZ, task.queuedEdgeChecksSky);
                }
                if (task.queuedEdgeChecksBlock != null) {
                    blockEngine.checkChunkEdges(this.chunkManager, chunkX, chunkZ, task.queuedEdgeChecksBlock);
                }

                task.onComplete.complete(null);
            }
        } finally {
            this.releaseSkyEngine(skyEngine);
            this.releaseBlockEngine(blockEngine);
        }
    }

    public int relight(final Set<ChunkPosition> positions, final Consumer<ChunkPosition> callback, final IntConsumer onComplete) {
        final Set<ChunkPosition> chunks = new LinkedHashSet<>(positions);
        int totalChunks = 0;
        for (final Iterator<ChunkPosition> iterator = chunks.iterator(); iterator.hasNext();) {
            final ChunkPosition position = iterator.next();
            final KryptonChunk chunk = world.getChunkManager().get(position.getX(), position.getZ());
            if (chunk == null || !chunk.isLightOn()) {
                iterator.remove();
                continue;
            }
            totalChunks++;
        }
        EXECUTOR.submit(() -> {
            relightChunks(chunks, callback, onComplete);
        });
        scheduleUpdates();
        return totalChunks;
    }

    public void relightChunks(final Set<ChunkPosition> positions, final Consumer<ChunkPosition> callback, final IntConsumer onComplete) {
        final SkyStarLightEngine skyEngine = getSkyEngine();
        final BlockStarLightEngine blockEngine = getBlockEngine();
        try {
            if (skyEngine != null) skyEngine.relightChunks(chunkManager, positions, callback, onComplete);
            blockEngine.relightChunks(chunkManager, positions, callback, onComplete);
        } finally {
            releaseSkyEngine(skyEngine);
            releaseBlockEngine(blockEngine);
        }
    }

    protected static final class LightQueue {

        protected final Long2ObjectLinkedOpenHashMap<ChunkTasks> chunkTasks = new Long2ObjectLinkedOpenHashMap<>();
        protected final StarLightManager manager;

        public LightQueue(final StarLightManager manager) {
            this.manager = manager;
        }

        public synchronized boolean isEmpty() {
            return this.chunkTasks.isEmpty();
        }

        public synchronized CompletableFuture<Void> queueBlockChange(final Vector3i pos) {
            final ChunkTasks tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(pos), ChunkTasks::new);
            tasks.changedPositions.add(pos);
            return tasks.onComplete;
        }

        public synchronized @NotNull CompletableFuture<@NotNull Void> queueSectionChange(final int x, final int y, final int z, final boolean newEmptyValue) {
            final ChunkTasks tasks = this.chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(x, z), ChunkTasks::new);

            if (tasks.changedSectionSet == null) {
                tasks.changedSectionSet = new Boolean[this.manager.maxSection - this.manager.minSection + 1];
            }
            tasks.changedSectionSet[y - this.manager.minSection] = newEmptyValue;

            return tasks.onComplete;
        }

        public synchronized @NotNull CompletableFuture<@NotNull Void> queueChunkLighting(final ChunkPosition position, final Runnable task) {
            final ChunkTasks tasks = chunkTasks.computeIfAbsent(CoordinateUtils.getChunkKey(position), ChunkTasks::new);
            if (tasks.lightTasks == null) tasks.lightTasks = new ArrayList<>();
            tasks.lightTasks.add(task);
            return tasks.onComplete;
        }

        public void removeChunk(final @NotNull ChunkPosition pos) {
            final ChunkTasks tasks;
            synchronized (this) {
                tasks = this.chunkTasks.remove(CoordinateUtils.getChunkKey(pos));
            }
            if (tasks != null) {
                tasks.onComplete.complete(null);
            }
        }

        public synchronized @Nullable ChunkTasks removeFirstTask() {
            if (this.chunkTasks.isEmpty()) {
                return null;
            }
            return this.chunkTasks.removeFirst();
        }

        protected static final class ChunkTasks {

            public final Set<Vector3i> changedPositions = new HashSet<>();
            public Boolean[] changedSectionSet;
            public ShortOpenHashSet queuedEdgeChecksSky;
            public ShortOpenHashSet queuedEdgeChecksBlock;
            public List<Runnable> lightTasks;

            public final CompletableFuture<Void> onComplete = new CompletableFuture<>();

            public final long chunkCoordinate;

            public ChunkTasks(final long chunkCoordinate) {
                this.chunkCoordinate = chunkCoordinate;
            }
        }
    }
}
