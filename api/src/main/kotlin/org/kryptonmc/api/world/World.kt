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
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.EntityType
import org.kryptonmc.api.space.Vector
import org.kryptonmc.api.world.chunk.Chunk
import org.kryptonmc.api.world.dimension.DimensionType
import org.kryptonmc.api.world.rule.GameRuleHolder
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
     * Gets a block at the given [x], [y], and [z] coordinates.
     *
     * Will return [void air][org.kryptonmc.api.block.Blocks.VOID_AIR] if the
     * coordinates exceed the world height, or
     * [air][org.kryptonmc.api.block.Blocks.AIR] if there is no chunk loaded
     * at the given coordinates.
     *
     * @param x the X coordinate of the block
     * @param y the Y coordinate of the block
     * @param z the Z coordinate of the block
     * @return see above
     */
    fun getBlock(x: Int, y: Int, z: Int): Block

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
    fun <T : Entity> spawnEntity(type: EntityType<T>, location: Vector)

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
