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
package org.kryptonmc.krypton.world.chunk

import ca.spottedleaf.starlight.StarLightEngine
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.api.space.Position
import org.kryptonmc.api.world.Biome
import org.kryptonmc.api.world.chunk.Chunk
import org.kryptonmc.krypton.util.logger
import org.kryptonmc.krypton.world.BlockAccessor
import org.kryptonmc.krypton.world.Heightmap
import org.kryptonmc.krypton.world.KryptonWorld
import org.kryptonmc.krypton.world.light.LightLayer
import org.spongepowered.math.vector.Vector3i
import java.util.BitSet
import java.util.EnumMap

class KryptonChunk(
    override val world: KryptonWorld,
    val position: ChunkPosition,
    val sections: Array<ChunkSection?>,
    override val biomes: List<Biome>,
    override var lastUpdate: Long,
    override var inhabitedTime: Long,
    val carvingMasks: Pair<ByteArray, ByteArray>,
    val structures: NBTCompound
) : Chunk, BlockAccessor {

    val heightmaps = EnumMap<Heightmap.Type, Heightmap>(Heightmap.Type::class.java)
    val lightEngine = world.chunkManager.lightEngine

    override val height = world.height
    override val minimumBuildHeight = world.minimumBuildHeight

    override val x = position.x
    override val z = position.z

    val blockChangedLightSectionFilter = BitSet()
    val skyChangedLightSectionFilter = BitSet()
    var isLightOn = true

    @Volatile var blockNibbles = StarLightEngine.getFilledEmptyLight(world)
    @Volatile var skyNibbles = StarLightEngine.getFilledEmptyLight(world)
    @Volatile var skyEmptinessMap: BooleanArray? = null
    @Volatile var blockEmptinessMap: BooleanArray? = null

    override fun getBlock(x: Int, y: Int, z: Int): Block {
        val sectionIndex = sectionIndex(y)
        if (sectionIndex in sections.indices) {
            val section = sections[sectionIndex]
            if (section != null && !section.isEmpty()) return section[x and 15, y and 15, z and 15]
        }
        return Blocks.AIR
    }

    override fun getBlock(position: Position) = getBlock(position.blockX, position.blockY, position.blockZ)

    override fun getBlock(position: Vector3i) = getBlock(position.x(), position.y(), position.z())

    override fun setBlock(x: Int, y: Int, z: Int, block: Block): Block? {
        // Get the section
        val sectionIndex = sectionIndex(y)
        var section = sections[sectionIndex]
        if (section == null) {
            if (block.isAir) return null
            section = ChunkSection(y shr 4)
            sections[sectionIndex] = section
        }

        // Get the local coordinates and set the new state in the section
        val localX = x and 15
        val localY = y and 15
        val localZ = z and 15
        val oldState = section.set(localX, localY, localZ, block)
        if (oldState == block) return null

        // Update the heightmaps
        heightmaps.getValue(Heightmap.Type.MOTION_BLOCKING).update(localX, y, localZ, block)
        heightmaps.getValue(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).update(localX, y, localZ, block)
        heightmaps.getValue(Heightmap.Type.OCEAN_FLOOR).update(localX, y, localZ, block)
        heightmaps.getValue(Heightmap.Type.WORLD_SURFACE).update(localX, y, localZ, block)
        return oldState
    }

    fun tick(playerCount: Int) {
        inhabitedTime += playerCount
    }

    fun setHeightmap(type: Heightmap.Type, data: LongArray) = heightmaps.getOrPut(type) { Heightmap(this, type) }.setData(this, type, data)

    fun onLightUpdate(layer: LightLayer, y: Int) {
        val bottomSection = lightEngine.minLightSection
        val topSection = lightEngine.maxLightSection
        if (y !in bottomSection..topSection) return
        val actual = y - bottomSection
        when (layer) {
            LightLayer.SKY -> skyChangedLightSectionFilter.set(actual)
            LightLayer.BLOCK -> blockChangedLightSectionFilter.set(actual)
        }
    }

    val highestSection: ChunkSection?
        get() {
            for (i in sections.size - 1 downTo 0) {
                val section = sections[i]
                if (section != null && !section.isEmpty()) return section
            }
            return null
        }

    val highestSectionPosition: Int
        get() = highestSection?.y ?: minimumBuildHeight

    companion object {

        private val LOGGER = logger<KryptonChunk>()
    }
}

// TODO: Do things with these
//data class TileTick(
//    val block: Block,
//    val priority: Int,
//    val delay: Int
//)
//
//data class StructureData(
//    val references: Map<String, Vector>,
//    val starts: List<Structure>
//)
