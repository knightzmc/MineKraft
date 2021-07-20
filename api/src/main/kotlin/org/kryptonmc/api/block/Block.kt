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
import org.kryptonmc.api.item.ItemLike

/**
 * Represents a block with certain properties.
 *
 * These are immutable and do not contain any state-specific
 * information, such as the world or location they are in, so
 * they can be easily reused in many places, which from a
 * technical standpoint, reduces allocations, but also makes
 * them much more thread-safe.
 */
interface Block : ItemLike, Comparable<Block> {

    /**
     * The key associated with this block.
     */
    val key: Key

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
     * If this block can be replaced.
     */
    val isReplaceable: Boolean

    /**
     * If this block has an associated block entity.
     */
    val hasBlockEntity: Boolean

    /**
     * If light cannot pass through this block.
     */
    val occludes: Boolean

    /**
     * If this block has a dynamic shape.
     */
    val hasDynamicShape: Boolean

    /**
     * If the shape of this block should be used to calculate light occlusion.
     */
    val useShapeForOcclusion: Boolean

    /**
     * If this block propagates skylight down.
     */
    val propagatesSkylightDown: Boolean

    /**
     * The amount of light that this block will block from passing through it.
     */
    val lightBlock: Int

    /**
     * If this block is sometimes fully opaque.
     */
    val isConditionallyFullyOpaque: Boolean

    /**
     * If this block is rendered solid.
     */
    val isSolidRender: Boolean

    /**
     * The opacity of this block.
     */
    val opacity: Int

    /**
     * If this block cannot be moved through.
     */
    val blocksMotion: Boolean

    /**
     * If this block has gravity.
     */
    val hasGravity: Boolean

    /**
     * If this block can be respawned inside of.
     */
    val canRespawnIn: Boolean

    /**
     * If the shape used for collision is very large.
     */
    val hasLargeCollisionShape: Boolean

    /**
     * If this block requires the correct tool to be used to break it.
     */
    val requiresCorrectTool: Boolean

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
