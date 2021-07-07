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
package org.kryptonmc.krypton.world.block

import net.kyori.adventure.text.Component
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.api.block.BoundingBox
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.api.space.Direction
import org.kryptonmc.krypton.registry.block.BlockData

class KryptonBlock(
    data: BlockData,
    override val properties: Map<String, String>
) : Block {

    override val key = data.key
    override val name = data.name
    override val id = data.id
    override val stateId = data.stateId
    override val hardness = data.hardness
    override val explosionResistance = data.resistance
    override val friction = data.friction
    override val speedFactor = data.speedFactor
    override val jumpFactor = data.jumpFactor
    override val isAir = data.air
    override val isSolid = data.solid
    override val isLiquid = data.liquid
    override val hasBlockEntity = data.blockEntity
    override val blocksMotion = data.blocksMotion
    override val hasGravity = data.gravity
    override val isFlammable = data.flammable
    override val lightEmission = data.lightEmission
    override val occludes = data.occludes
    override val translation = Component.translatable(data.translationKey)

    private val shapes = data.shapes
    private val occlusionShapes = data.occlusionShapes

    // Light stuff
    private val isTransparentOnSomeFaces by lazy {
        id == Blocks.DAYLIGHT_DETECTOR.id ||
                id == Blocks.DIRT_PATH.id ||
                id == Blocks.ENCHANTING_TABLE.id ||
                id == Blocks.END_PORTAL_FRAME.id ||
                id == Blocks.FARMLAND.id ||
                id == Blocks.LECTERN.id ||
                id == Blocks.PISTON.id && stateId == Blocks.PISTON.withProperty("extended", "true").stateId ||
                id == Blocks.PISTON_HEAD.id ||
                id == Blocks.SCULK_SENSOR.id ||
                name.contains("SLAB") && getProperty("type")?.equals("double", true) == false ||
                id == Blocks.SNOW.id && getProperty("layers")?.equals("8") == false ||
                name.contains("STAIRS") ||
                id == Blocks.STONECUTTER.id
    }

    private val hasDynamicShape = name.contains("SHULKER_BOX")
    private val isFullBlock = shapes.firstOrNull() ?: BoundingBox.EMPTY == BoundingBox.BLOCK
    private val isSolidRender = occludes && occlusionShapes.firstOrNull() ?: BoundingBox.EMPTY == BoundingBox.BLOCK
    private val propagatesSkyLightDown = !isFullBlock && !isLiquid

    override val blockedLight = if (isSolidRender) MAXIMUM_LIGHT_LEVEL else if (propagatesSkyLightDown) 0 else 1
    override val isConditionallyFullyOpaque by lazy { isTransparentOnSomeFaces and occludes }
    override val opacity by lazy { if (hasDynamicShape || isConditionallyFullyOpaque) -1 else blockedLight }

    init {
        Registries.register(Registries.BLOCK, key, this)
    }

    override fun faceOcclusionBox(direction: Direction) = occlusionShapes.getOrNull(direction.id) ?: BoundingBox.BLOCK

    override fun withProperty(key: String, value: String): Block {
        val properties = properties.toMutableMap().apply { put(key, value) }
        return requireNotNull(KryptonBlockLoader.properties(this.key.asString(), properties)) { "Invalid properties: $key:$value" }
    }

    override fun compareTo(other: Block) = id.compareTo(other.id)

    companion object {

        private const val MAXIMUM_LIGHT_LEVEL = 15
    }
}
