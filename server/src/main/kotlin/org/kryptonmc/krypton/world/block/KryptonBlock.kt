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
import org.kryptonmc.api.item.ItemType
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.krypton.registry.block.BlockData

class KryptonBlock(
    data: BlockData,
    override val properties: Map<String, String>
) : Block {

    override val key = data.key
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
    override val canRespawnIn = data.canRespawnIn
    override val isReplaceable = data.replaceable
    override val hasDynamicShape = data.dynamicShape
    override val useShapeForOcclusion = data.useShapeForOcclusion
    override val propagatesSkylightDown = data.propagatesLightDown
    override val hasLargeCollisionShape = data.largeCollisionShape
    override val isConditionallyFullyOpaque = data.conditionallyFullyOpaque
    override val isSolidRender = data.solidRender
    override val lightBlock = data.lightBlock
    override val opacity = data.opacity
    override val requiresCorrectTool = data.toolRequired
    override val translation = Component.translatable(data.translationKey)
    private val itemKey = data.itemKey

    init {
        Registries.register(Registries.BLOCK, key, this)
    }

    override fun withProperty(key: String, value: String): Block {
        val properties = properties.toMutableMap().apply { put(key, value) }
        return requireNotNull(KryptonBlockLoader.properties(this.key.asString(), properties)) { "Invalid properties: $key:$value" }
    }

    override fun asItem() = itemKey?.let { Registries.ITEM[it] }

    override fun compareTo(other: Block) = id.compareTo(other.id)
}
