/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.world

import net.kyori.adventure.audience.ForwardingAudience
import org.kryptonmc.api.Server
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.EntityType
import org.kryptonmc.api.space.Position
import org.kryptonmc.api.space.Vector
import org.kryptonmc.api.world.chunk.Chunk
import org.kryptonmc.api.world.dimension.DimensionType
import org.kryptonmc.api.world.rule.GameRuleHolder
import org.spongepowered.math.vector.Vector2i
import org.spongepowered.math.vector.Vector3i
import java.nio.file.Path
import java.util.UUID

/**
 * Represents a loaded world.
 */
interface World : ForwardingAudience {

    /**
     * The server this world was loaded on.
     */
    val server: Server

    /**
     * The name of this world.
     */
    val name: String

    /**
     * The unique ID of this world.
     */
    val uuid: UUID

    /**
     * The folder of this world on disk.
     */
    val folder: Path

    /**
     * The dimension that this world is.
     */
    val dimensionType: DimensionType

    /**
     * The spawn location of this world.
     *
     * As spawn locations do not allow for coordinates with fractional
     * precision, the x, y and z values of this [Location] will always
     * be whole numbers (integers). They will never possess a decimal
     * component.
     *
     * Spawn locations also do not possess any rotational components, so
     * the pitch and yaw of this [Location] will always be set to 0.
     */
    val spawnLocation: Vector3i

    /**
     * The set of chunks currently loaded in this world.
     */
    val chunks: Collection<Chunk>

    /**
     * This world's border.
     */
    val border: WorldBorder

    /**
     * The difficulty of this world.
     */
    val difficulty: Difficulty

    /**
     * The default gamemode of this world.
     */
    val gamemode: Gamemode

    /**
     * If the world is a hardcore world.
     */
    val isHardcore: Boolean

    /**
     * The seed of this world.
     */
    val seed: Long

    /**
     * The current time in this world.
     */
    val time: Long

    /**
     * The version information of this world.
     */
    val version: GameVersion

    /**
     * The maximum build height of this world. Also known as the build limit.
     */
    val maxHeight: Int

    /**
     * If this world is currently thundering (has an ongoing thunderstorm).
     */
    var isThundering: Boolean

    /**
     * The level of the current thunderstorm (0 if there is no thunderstorm going on).
     */
    var thunderLevel: Float

    /**
     * If this world is currently raining.
     */
    var isRaining: Boolean

    /**
     * The level of the current rain.
     */
    var rainLevel: Float

    /**
     * The game rules for this world.
     */
    val gameRules: GameRuleHolder

    /**
     * Gets the block at the given coordinates.
     *
     * This function will return the following in specific cases:
     * - If the given [y] coordinate is greater than the maximum height of this world,
     * this will return [Blocks.VOID_AIR].
     * - If there is no chunk loaded at the given coordinates (`getChunkAt` was null),
     * this will return [Blocks.AIR].
     * - Else it will return the block at the given coordinates.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return see above
     */
    fun getBlock(x: Int, y: Int, z: Int): Block

    /**
     * Gets the block at the given coordinates.
     *
     * This function will return the following in specific cases:
     * - If the given [position]'s [Vector3i.y] coordinate is greater than the
     * maximum height of this world, this will return [Blocks.VOID_AIR].
     * - If there is no chunk loaded at the given coordinates (`getChunkAt` was null),
     * this will return [Blocks.AIR].
     * - Else it will return the block at the given coordinates.
     *
     * @param position the position of the block
     * @return see above
     */
    fun getBlock(position: Vector3i): Block = getBlock(position.x(), position.y(), position.z())

    /**
     * Gets the block at the given coordinates.
     *
     * This function will return the following in specific cases:
     * - If the given [position]'s [Position.blockY] coordinate is greater than the
     * maximum height of this world, this will return [Blocks.VOID_AIR].
     * - If there is no chunk loaded at the given coordinates (`getChunkAt` was null),
     * this will return [Blocks.AIR]
     * - Else it will return the block at the given coordinates
     *
     * @param position the position
     * @return see above
     */
    fun getBlock(position: Position): Block = getBlock(position.blockX, position.blockY, position.blockZ)

    /**
     * Sets the block at the given coordinates to the given [block].
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param block the new block
     */
    fun setBlock(x: Int, y: Int, z: Int, block: Block)

    /**
     * Sets the block at the given [position] to the given [block].
     *
     * @param position the position
     * @param block the new block
     */
    fun setBlock(position: Vector3i, block: Block) = setBlock(position.x(), position.y(), position.z(), block)

    /**
     * Sets the block at the given [position] to the given [block].
     *
     * @param position the position
     * @param block the block
     */
    fun setBlock(position: Position, block: Block) = setBlock(position.blockX, position.blockY, position.blockZ, block)

    /**
     * Gets a chunk from its **chunk** coordinates, or returns null if there is
     * no chunk **loaded** at the given coordinates.
     *
     * That is, to calculate the chunk coordinate from a given block coordinate,
     * the block coordinate is shifted right by 4 (divided by 16 and floored).
     *
     * @param x the chunk X coordinate
     * @param z the chunk Z coordinate
     * @return the chunk at the given coordinates, or null if there isn't one loaded
     */
    fun getChunkAt(x: Int, z: Int): Chunk?

    /**
     * Gets a chunk from its **chunk** coordinates, or returns null if there is
     * no chunk **loaded** at the given coordinates.
     *
     * That is, to calculate the chunk coordinate from a given block coordinate,
     * the block coordinate is shifted right by 4 (divided by 16 and floored).
     *
     * @param position the chunk position
     * @return the chunk at the given position, or null if there isn't one loaded
     */
    fun getChunkAt(position: Vector2i): Chunk? = getChunkAt(position.x(), position.y())

    /**
     * Gets a chunk from its **block** coordinates, or returns null if there is
     * no chunk **loaded** at the given coordinates.
     *
     * @param x the block X coordinate
     * @param y the block Y coordinate
     * @param z the block Z coordinate
     * @return the chunk at the given coordinates, or null if there isn't one loaded
     */
    fun getChunk(x: Int, y: Int, z: Int): Chunk?

    /**
     * Gets a chunk from its **block** coordinates, or returns null if there is
     * no chunk **loaded** at the given coordinates.
     *
     * @param position the block position
     * @return the chunk at the given coordinates, or null if there isn't one loaded
     */
    fun getChunk(position: Vector3i): Chunk? = getChunk(position.x(), position.y(), position.z())

    /**
     * Gets a chunk from its **block** coordinates, or returns null if there is
     * no chunk **loaded** at the given coordinates.
     *
     * @param position the position
     * @return the chunk at the given coordinates, or null if there isn't one loaded
     */
    fun getChunk(position: Position): Chunk? = getChunk(position.blockX, position.blockY, position.blockZ)

    /**
     * Gets or loads the chunk at the given **chunk** coordinates.
     *
     * That is, to calculate the chunk coordinate from a given block coordinate,
     * the block coordinate is shifted right by 4 (divided by 16 and floored).
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     */
    fun loadChunk(x: Int, z: Int): Chunk

    /**
     * Saves this world to disk. Exposed as a function of [World] to allow for custom world implementations
     * to define this.
     */
    fun save()

    /**
     * Spawns an entity with the given [type] in this world at the given [location].
     *
     * @param type the type of the entity
     * @param location the location to spawn the entity at
     */
    fun <T : Entity> spawnEntity(type: EntityType<T>, location: Vector): T?

    /**
     * Spawns an experience orb in this world at the given [location].
     */
    fun spawnExperienceOrb(location: Vector)

    /**
     * Spawns a painting in this world at the given [location].
     */
    fun spawnPainting(location: Vector)
}

/**
 * Holder for information about the version of Minecraft this world was
 * generated in.
 *
 * @param id the ID of the world version
 * @param name the name of the Minecraft version
 * @param isSnapshot whether this world version is a snapshot version or not
 */
class GameVersion(
    val id: Int,
    val name: String,
    val isSnapshot: Boolean
) {

    override fun equals(other: Any?) = other is GameVersion && id == other.id && isSnapshot == other.isSnapshot

    override fun hashCode() = id + if (isSnapshot) 1231 else 1237 // java.lang.Boolean.hashCode implementation

    override fun toString() = "WorldVersion(id=$id, name=$name, isSnapshot=$isSnapshot)"
}
