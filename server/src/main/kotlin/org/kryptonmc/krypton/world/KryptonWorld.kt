/*
 * This file is part of the Krypton project, licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kryptonmc.krypton.world

import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.kyori.adventure.sound.Sound
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTString
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.jglrxavpok.hephaistos.nbt.NBTWriter
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.api.effect.sound.SoundEvent
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.EntityType
import org.kryptonmc.api.event.entity.EntityRemoveEvent
import org.kryptonmc.api.event.entity.EntitySpawnEvent
import org.kryptonmc.api.registry.RegistryKey
import org.kryptonmc.api.space.Position
import org.kryptonmc.api.space.Vector
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.api.world.Difficulty
import org.kryptonmc.api.world.World
import org.kryptonmc.api.world.GameVersion
import org.kryptonmc.api.world.rule.GameRuleHolder
import org.kryptonmc.api.world.rule.GameRules
import org.kryptonmc.krypton.KryptonServer.KryptonServerInfo
import org.kryptonmc.krypton.ServerInfo
import org.kryptonmc.krypton.entity.EntityFactory
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.out.play.GameState
import org.kryptonmc.krypton.packet.out.play.PacketOutChangeGameState
import org.kryptonmc.krypton.util.createTempFile
import org.kryptonmc.krypton.util.csv.csv
import org.kryptonmc.krypton.util.profiling.Profiler
import org.kryptonmc.krypton.world.chunk.ChunkManager
import org.kryptonmc.krypton.world.chunk.KryptonChunk
import org.kryptonmc.api.world.dimension.DimensionTypes
import org.kryptonmc.krypton.effect.Effect
import org.kryptonmc.krypton.packet.out.play.PacketOutBlockChange
import org.kryptonmc.krypton.packet.out.play.PacketOutSoundEffect
import org.kryptonmc.krypton.packet.out.play.PacketOutEffect
import org.kryptonmc.krypton.packet.out.play.PacketOutTimeUpdate
import org.kryptonmc.krypton.registry.InternalRegistryKeys
import org.kryptonmc.krypton.util.KEY_CODEC
import org.kryptonmc.krypton.util.clamp
import org.kryptonmc.krypton.util.forEachInRange
import org.kryptonmc.krypton.util.synchronize
import org.kryptonmc.krypton.world.chunk.ChunkPosition
import org.kryptonmc.krypton.world.generation.WorldGenerationSettings
import org.spongepowered.math.vector.Vector3i
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream
import kotlin.random.Random

data class KryptonWorld(
    override val server: KryptonServer,
    override val folder: Path,
    override val uuid: UUID,
    override val name: String,
    val allowCheats: Boolean,
    override val border: KryptonWorldBorder,
    var clearWeatherTime: Int,
    var dayTime: Long,
    override val difficulty: Difficulty,
    //val endDimensionData: EndDimensionData, // for the end, when it is supported
    override val gameRules: GameRuleHolder,
    val generationSettings: WorldGenerationSettings,
    override var gamemode: Gamemode,
    override val isHardcore: Boolean,
    //val isInitialized: Boolean, // we always assume this is a complete world
    val lastPlayed: LocalDateTime,
    val mapFeatures: Boolean,
    override var isRaining: Boolean,
    var rainTime: Int,
    override val spawnLocation: Vector3i,
    val spawnAngle: Float,
    override var isThundering: Boolean,
    var thunderTime: Int,
    override var time: Long,
    val nbtVersion: Int,
    override val version: GameVersion,
    override val maxHeight: Int,
    val serverBrands: MutableSet<String>,
) : World, BlockAccessor {

    override val seed = generationSettings.seed

    val chunkMap = Long2ObjectOpenHashMap<KryptonChunk>().synchronize()
    val players: MutableSet<KryptonPlayer> = ConcurrentHashMap.newKeySet()
    val entities: MutableSet<KryptonEntity> = ConcurrentHashMap.newKeySet()
    private val entitiesByChunk = object : ConcurrentHashMap<Long, MutableSet<KryptonEntity>>() {
        override fun get(key: Long) = super.computeIfAbsent(key) { newKeySet() }
        operator fun get(chunk: KryptonChunk) = get(ChunkPosition.toLong(chunk.position.x, chunk.position.z))
    }

    val chunkManager = ChunkManager(this)
    val playerManager = server.playerManager
    val dimension = OVERWORLD
    override val dimensionType = DimensionTypes.OVERWORLD

    override val height = dimensionType.height
    override val minimumBuildHeight = dimensionType.minimumY

    private var oldRainLevel = 0F
    override var rainLevel = 0F
    private var oldThunderLevel = 0F
    override var thunderLevel = 0F

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> spawnEntity(type: EntityType<T>, location: Vector): T? {
        if (!type.isSummonable) return null
        // TODO: Fix this when the rest of the entity types exist again
//        when (type) {
//            EntityType.PLAYER -> return // TODO: Implement player spawning
//            EntityType.EXPERIENCE_ORB -> spawnExperienceOrb(location)
//            EntityType.PAINTING -> spawnPainting(location)
//        }
        val entity = EntityFactory.create(type, this)?.apply { this.location = location.toLocation(0F, 0F) } ?: return null
        spawnEntity(entity)
        return entity as? T
    }

    fun spawnEntity(entity: KryptonEntity) {
        if (entity.world != this) return
        server.eventManager.fire(EntitySpawnEvent(entity, this)).thenAccept { event ->
            if (!event.result.isAllowed) return@thenAccept
            val location = entity.location

            if (entity is KryptonPlayer) {
                // TODO: World border
                entity.session.sendPacket(PacketOutTimeUpdate(time, dayTime))
            }
            forEachInRange(location, server.config.world.viewDistance) {
                if (entity is KryptonPlayer) it.addViewer(entity)
                if (it is KryptonPlayer) entity.addViewer(it)
            }

            val chunk = getChunk(entity.location) ?: return@thenAccept
            entitiesByChunk[chunk.position.toLong()].add(entity)
            entities.add(entity)
        }
    }

    fun removeEntity(entity: KryptonEntity) {
        if (entity.world != this) return
        server.eventManager.fire(EntityRemoveEvent(entity, this)).thenAccept {
            if (!it.result.isAllowed) return@thenAccept
            entity.viewers.forEach(entity::removeViewer)
            val chunk = getChunk(entity.location) ?: return@thenAccept
            entitiesByChunk[chunk.position.toLong()].remove(entity)
            entities.remove(entity)
        }
    }

    override fun spawnExperienceOrb(location: Vector) = Unit // TODO: Implement XP orb spawning

    override fun spawnPainting(location: Vector) = Unit // TODO: Implement painting spawning

    fun playEffect(effect: Effect, position: Vector3i, data: Int, except: KryptonPlayer) = playerManager.broadcast(PacketOutEffect(effect, position, data, false), this, position, 64.0, except)

    fun playSound(
        position: Vector3i,
        sound: Sound,
        event: SoundEvent,
        except: KryptonPlayer
    ) = playSound(position.x().toDouble() + 0.5, position.y().toDouble() + 0.5, position.z().toDouble() + 0.5, sound, event, except)

    private fun playSound(
        x: Double,
        y: Double,
        z: Double,
        sound: Sound,
        event: SoundEvent,
        except: KryptonPlayer
    ) = playerManager.broadcast(PacketOutSoundEffect(sound, event, x, y, z), this, x, y, z, if (sound.volume() > 1F) (16F * sound.volume()).toDouble() else 16.0, except)

    override fun getBlock(x: Int, y: Int, z: Int): Block {
        if (y.outsideBuildHeight) return Blocks.VOID_AIR
        val chunk = getChunkAt(x, z) ?: return Blocks.AIR
        return chunk.getBlock(x, y, z)
    }

    override fun getChunkAt(x: Int, z: Int) = chunkManager[x, z]

    override fun getChunk(x: Int, y: Int, z: Int) = getChunkAt(x shr 4, z shr 4)

    override fun getChunk(position: Position) = getChunk(position.blockX, position.blockY, position.blockZ)

    override fun getChunk(position: Vector3i) = getChunk(position.x(), position.y(), position.z())

    fun getEntitiesInChunk(chunk: KryptonChunk) = entitiesByChunk[chunk.position.toLong()]

    override fun loadChunk(x: Int, z: Int) = chunkManager.load(x, z)

    override fun setBlock(x: Int, y: Int, z: Int, block: Block) {
        if (y.outsideBuildHeight) return
        getChunkAt(x shr 4, z shr 4)?.setBlock(x, y, z, block)?.run {
            playerManager.sendToAll(PacketOutBlockChange(block, x, y, z), this@KryptonWorld)
        }
    }

    fun tick(profiler: Profiler) {
        if (players.isEmpty()) return // don't tick the world if there's no players in it

        profiler.push("increment time")
        time++
        dayTime++
        profiler.pop()

        // tick rain
        profiler.push("weather")
        val raining = isRaining
        if (dimensionType.hasSkylight) {
            if (gameRules[GameRules.DO_WEATHER_CYCLE]) {
                if (clearWeatherTime > 0) {
                    --clearWeatherTime
                    thunderTime = if (isThundering) 0 else 1
                    rainTime = if (isRaining) 0 else 1
                    isThundering = false
                    isRaining = false
                } else {
                    if (thunderTime > 0) {
                        if (thunderTime-- == 0) isThundering = !isThundering
                    } else {
                        thunderTime = if (isThundering) Random.nextInt(12000) + 3600 else Random.nextInt(168000) + 12000
                    }
                    if (rainTime > 0) {
                        if (rainTime-- == 0) isRaining = !isRaining
                    } else {
                        rainTime = Random.nextInt(if (isRaining) 12000 else 168000) + 12000
                    }
                }
            }
            oldThunderLevel = thunderLevel
            thunderLevel = if (isThundering) (thunderLevel + 0.01).toFloat() else (thunderLevel - 0.01).toFloat()
            thunderLevel = thunderLevel.clamp(0F, 1F)
            oldRainLevel = rainLevel
            rainLevel = if (isRaining) (rainLevel + 0.01).toFloat() else (rainLevel - 0.01).toFloat()
            rainLevel = rainLevel.clamp(0F, 1F)
        }

        if (oldRainLevel != rainLevel) playerManager.sendToAll(PacketOutChangeGameState(GameState.RAIN_LEVEL_CHANGE, rainLevel), this)
        if (oldThunderLevel != thunderLevel) playerManager.sendToAll(PacketOutChangeGameState(GameState.THUNDER_LEVEL_CHANGE, thunderLevel), this)
        if (raining != isRaining) {
            playerManager.sendToAll(PacketOutChangeGameState(if (raining) GameState.END_RAINING else GameState.BEGIN_RAINING))
            playerManager.sendToAll(PacketOutChangeGameState(GameState.RAIN_LEVEL_CHANGE, rainLevel))
            playerManager.sendToAll(PacketOutChangeGameState(GameState.THUNDER_LEVEL_CHANGE, thunderLevel))
        }
        profiler.pop()

        profiler.push("chunk tick")
        chunkMap.values.forEach { chunk -> chunk.tick(players.count { it.location in chunk.position }) }
        profiler.pop()
    }

    fun saveDebugReport(path: Path) {
        val chunksPath = path.resolve("chunks.csv")
        Files.newBufferedWriter(chunksPath).use { it.dumpChunks() }
    }

    override fun save() {
        val gamerules = gameRules.rules.transform { (rule, value) -> rule.name to NBTString(value.toString()) }
        val dimensions = generationSettings.dimensions.transform { (key, value) -> key.toString() to value.toNBT() }

        val data = NBTCompound().set("Data", NBTCompound()
            .setBoolean("allowCommands", false)
            .setDouble("BorderCenterX", border.center.x())
            .setDouble("BorderCenterZ", border.center.y())
            .setDouble("BorderDamagePerBlock", border.damageMultiplier)
            .setDouble("BorderSize", border.size)
            .setDouble("BorderSafeZone", border.safeZone)
            .setDouble("BorderSizeLerpTarget", border.sizeLerpTarget)
            .setLong("BorderSizeLerpTime", border.sizeLerpTime)
            .setDouble("BorderWarningBlocks", border.warningBlocks.toDouble())
            .setDouble("BorderWarningTime", border.warningTime.toDouble())
            .setInt("clearWeatherTime", clearWeatherTime)
            .set("CustomBossEvents", NBTCompound())
            .set("DataPacks", NBTCompound()
                .set("Enabled", NBTList<NBTString>(NBTTypes.TAG_String).apply { add(NBTString("vanilla")) }))
                .set("Disabled", NBTList<NBTString>(NBTTypes.TAG_String))
            .setInt("DataVersion", ServerInfo.WORLD_VERSION)
            .setLong("DayTime", dayTime)
            .setByte("Difficulty", difficulty.ordinal.toByte())
            .set("GameRules", NBTCompound().apply { gamerules.forEach { (key, value) -> set(key, value) } })
            .set("WorldGenSettings", NBTCompound()
                .setLong("seed", generationSettings.seed)
                .setBoolean("generate_features", generationSettings.generateStructures)
                .set("dimensions", NBTCompound().apply { dimensions.forEach { (key, value) -> set(key, value) } }))
            .setInt("GameType", gamemode.ordinal)
            .setBoolean("hardcore", isHardcore)
            .setBoolean("initialized", true)
            .set("Krypton", NBTCompound()
                .setString("version", KryptonServerInfo.version))
            .setLong("LastPlayed", lastPlayed.toInstant(ZoneOffset.UTC).toEpochMilli())
            .setString("LevelName", name)
            .setBoolean("MapFeatures", mapFeatures)
            .setBoolean("raining", isRaining)
            .setInt("rainTime", rainTime)
            .set("ServerBrands", NBTList<NBTString>(NBTTypes.TAG_String).apply { serverBrands.apply { add(KryptonServerInfo.name) }.forEach { add(NBTString(it)) } })
            .setInt("SpawnX", spawnLocation.x())
            .setInt("SpawnY", spawnLocation.y())
            .setInt("SpawnZ", spawnLocation.z())
            .setBoolean("thundering", isThundering)
            .setLong("Time", time)
            .setInt("version", NBT_VERSION)
            .set("Version", NBTCompound()
                .setInt("Id", ServerInfo.GAME_VERSION.id)
                .setString("Name", ServerInfo.GAME_VERSION.name)
                .setBoolean("Snapshot", ServerInfo.GAME_VERSION.isSnapshot))
            .setInt("WanderingTraderSpawnChance", 25)
            .setInt("WanderingTraderSpawnDelay", 24_000)
        )

        val temp = folder.createTempFile("level", ".dat")
        NBTWriter(temp.outputStream()).use { it.writeNamed("", data) }
        val dataPath = folder.resolve("level.dat")
        if (!dataPath.exists()) {
            temp.moveTo(dataPath)
            return
        }
        val oldDataPath = folder.resolve("level.dat_old").apply { deleteIfExists() }
        dataPath.moveTo(oldDataPath)
        dataPath.deleteIfExists()
        temp.moveTo(dataPath)
    }

    override fun audiences() = players

    override fun equals(other: Any?) = other is KryptonWorld && uuid == other.uuid

    override fun hashCode() = uuid.hashCode()

    override fun toString() = "KryptonWorld(uuid=$uuid,name=$name)"

    private fun Writer.dumpChunks() {
        val output = csv(this) {
            plus("x")
            plus("z")
            plus("world")
        }
        chunkMap.values.forEach { output.writeRow(it.position.x, it.position.z, it.world) }
    }

    override val chunks: Collection<KryptonChunk>
        get() = chunkMap.values

    companion object {

        val REGISTRY_KEY_CODEC: Codec<RegistryKey<KryptonWorld>> = KEY_CODEC.xmap({ RegistryKey(InternalRegistryKeys.DIMENSION, it) }, RegistryKey<KryptonWorld>::location)
        val OVERWORLD = RegistryKey(InternalRegistryKeys.DIMENSION, DimensionTypes.OVERWORLD_KEY)
        val THE_NETHER = RegistryKey(InternalRegistryKeys.DIMENSION, DimensionTypes.THE_NETHER_KEY)
        val THE_END = RegistryKey(InternalRegistryKeys.DIMENSION, DimensionTypes.THE_END_KEY)
    }
}

const val NBT_VERSION = 19_133
