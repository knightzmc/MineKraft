/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.block

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.TranslatableComponent
import org.jetbrains.annotations.Contract
import org.kryptonmc.api.space.Direction

/**
 * Represents a block.
 *
 * This does not store any specific information about the location or
 * the world of the block as it is designed to be reused.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
interface Block : Comparable<Block> {

    /**
     * The key associated with this block.
     */
    val key: Key

    /**
     * The enum name of this block.
     *
     * For example, if the [key] was "minecraft:air", the
     * name would likely be "AIR".
     */
    val name: String

    /**
     * The block ID of this block.
     */
    val id: Int

    /**
     * The ID of the block state this block represents.
     */
    val stateId: Int

    /**
     * The hardness of this block.
     */
    val hardness: Double

    /**
     * How resistant this block is to explosions. Higher
     * means more resistant.
     */
    val explosionResistance: Double

    /**
     * The amount of light this block emits, in levels.
     */
    val lightEmission: Int

    /**
     * The friction of this block.
     */
    val friction: Double

    /**
     * The speed factor of this block.
     */
    val speedFactor: Double

    /**
     * The jump factor of this block.
     */
    val jumpFactor: Double

    /**
     * If this block is air.
     */
    val isAir: Boolean

    /**
     * If this block is solid.
     */
    val isSolid: Boolean

    /**
     * If this block is liquid.
     */
    val isLiquid: Boolean

    /**
     * If this block is flammable (can be set on fire).
     */
    val isFlammable: Boolean

    /**
     * If this block has an associated block entity.
     */
    @get:JvmName("hasBlockEntity")
    val hasBlockEntity: Boolean

    /**
     * If light cannot pass through this block.
     */
    @get:JvmName("occludes")
    val occludes: Boolean

    /**
     * If this block cannot be moved through.
     */
    @get:JvmName("blocksMotion")
    val blocksMotion: Boolean

    /**
     * If this block has gravity.
     */
    @get:JvmName("hasGravity")
    val hasGravity: Boolean

    /**
     * If this block is opaque on some condition.
     *
     * For example, a piston is only opaque if it is
     * not extended.
     */
    val isConditionallyFullyOpaque: Boolean

    /**
     * The opacity of this block.
     */
    val opacity: Int

    /**
     * The amount of light blocked by this block.
     */
    val blockedLight: Int

    /**
     * The translation component for translating the name
     * of this block.
     */
    val translation: TranslatableComponent

    /**
     * This block's properties.
     */
    val properties: Map<String, String>

    /**
     * Gets the occlusion bounding box in a specific direction.
     *
     * @param direction the direction (face)
     * @return the occlusion bounding box in that direction
     */
    fun faceOcclusionBox(direction: Direction): BoundingBox

    /**
     * Gets the value of the property with the specified [key],
     * or null if there is no value associated with the given
     * [key].
     *
     * @param key the key
     * @return the value of the property, or null if not present
     */
    fun getProperty(key: String) = properties[key]

    /**
     * Creates a new [Block] with the property with key [key]
     * set to the value [value].
     *
     * @param key the key
     * @param value the value
     * @return a new block with the applied property
     */
    @Contract("_ -> new", pure = true)
    fun withProperty(key: String, value: String): Block

    /**
     * Creates a new [Block] with the given [properties] applied
     * to it.
     */
    @Contract("_ -> new", pure = true)
    fun withProperties(properties: Map<String, String>): Block {
        var block = this
        properties.forEach { block = block.withProperty(it.key, it.value) }
        return block
    }
}
