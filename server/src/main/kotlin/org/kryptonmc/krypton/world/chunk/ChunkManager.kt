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

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.kryptonmc.api.world.Biome
import org.kryptonmc.krypton.world.Heightmap
import org.kryptonmc.krypton.world.KryptonWorld
import org.kryptonmc.krypton.world.region.RegionFileManager
import java.util.EnumSet
import java.util.concurrent.TimeUnit

class ChunkManager(private val world: KryptonWorld) {

    private val regionFileManager = RegionFileManager(world.folder.resolve("region"), world.server.config.advanced.synchronizeChunkWrites)
    private val chunkCache: Cache<Long, KryptonChunk> = Caffeine.newBuilder()
        .maximumSize(512)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build()

    operator fun get(x: Int, z: Int): KryptonChunk? = chunkCache.getIfPresent(ChunkPosition.toLong(x, z))

    fun load(positions: List<ChunkPosition>): List<KryptonChunk> {
        val chunks = mutableListOf<KryptonChunk>()
        positions.forEach {
            val chunk = load(it.x, it.z)
            world.chunkMap[it.toLong()] = chunk
            chunks += chunk
        }
        return chunks
    }

    fun load(x: Int, z: Int): KryptonChunk {
        val cachedChunk = chunkCache.getIfPresent(ChunkPosition.toLong(x, z))
        if (cachedChunk != null) return cachedChunk

        val position = ChunkPosition(x, z)
        val nbt = regionFileManager.read(position).getCompound("Level")
        val heightmaps = nbt.getCompound("Heightmaps")

        val sectionList = nbt.getList<NBTCompound>("Sections")
        val sections = arrayOfNulls<ChunkSection>(world.sectionCount)
        for (i in sectionList.indices) {
            val sectionData = sectionList[i]
            val y = sectionData.getByte("Y").toInt()
            if (y == -1 || y == 16) continue
            if (sectionData.contains("Palette", NBTTypes.TAG_List) && sectionData.contains("BlockStates", NBTTypes.TAG_Long_Array)) {
                val section = ChunkSection(
                    y,
                    sectionData.getByteArray("BlockLight"),
                    sectionData.getByteArray("SkyLight")
                )
                section.palette.load(sectionData.getList("Palette"), sectionData.getLongArray("BlockStates"))
                section.recount()
                if (!section.isEmpty()) sections[world.sectionIndexFromY(y)] = section
            }
        }

        val carvingMasks = nbt.getCompound("CarvingMasks").let {
            it.getByteArray("AIR") to it.getByteArray("LIQUID")
        }
        val chunk =  KryptonChunk(
            world,
            position,
            sections,
            nbt.getIntArray("Biomes").map { Biome.fromId(it) },
            nbt.getLong("LastUpdate"),
            nbt.getLong("inhabitedTime"),
            carvingMasks,
            nbt.getCompound("Structures")
        )
        chunkCache.put(position.toLong(), chunk)

        val noneOf = EnumSet.noneOf(Heightmap.Type::class.java)
        Heightmap.Type.POST_FEATURES.forEach {
            if (heightmaps.contains(it.name, NBTTypes.TAG_Long_Array)) chunk.setHeightmap(it, heightmaps.getLongArray(it.name)) else noneOf.add(it)
        }
        Heightmap.prime(chunk, noneOf)
        return chunk
    }

    fun saveAll() = world.chunkMap.values.forEach { save(it) }

    fun save(chunk: KryptonChunk) {
        val lastUpdate = world.time
        chunkCache.invalidate(chunk.position.toLong())
        chunk.lastUpdate = lastUpdate
        regionFileManager.write(chunk.position, chunk.serialize())
    }
}

private fun KryptonChunk.serialize(): NBTCompound {
    val data = NBTCompound()
        .setIntArray("Biomes", biomes.map { it.id }.toIntArray())
        .set("CarvingMasks", NBTCompound()
            .setByteArray("AIR", carvingMasks.first)
            .setByteArray("LIQUID", carvingMasks.second))
        .setLong("LastUpdate", lastUpdate)
        .set("Lights", NBTList<NBT>(NBTTypes.TAG_List))
        .set("LiquidsToBeTicked", NBTList<NBT>(NBTTypes.TAG_List))
        .set("LiquidTicks", NBTList<NBT>(NBTTypes.TAG_List))
        .setLong("InhabitedTime", inhabitedTime)
        .set("PostProcessing", NBTList<NBT>(NBTTypes.TAG_List))
        .setString("Status", "full")
        .set("TileEntities", NBTList<NBT>(NBTTypes.TAG_Compound))
        .set("TileTicks", NBTList<NBT>(NBTTypes.TAG_Compound))
        .set("ToBeTicked", NBTList<NBT>(NBTTypes.TAG_List))
        .set("Structures", structures)
        .setInt("xPos", position.x)
        .setInt("zPos", position.z)

    val sectionList = NBTList<NBTCompound>(NBTTypes.TAG_Compound)
    for (i in minimumLightSection until maximumLightSection) {
        val section = sections.asSequence().filter { it != null && it.y shr 4 == i }.firstOrNull()
        if (section != null) sectionList.add(NBTCompound()
            .setByte("Y", (i and 255).toByte())
            .apply {
                section.palette.save(this)
                if (section.blockLight.isNotEmpty()) setByteArray("BlockLight", section.blockLight)
                if (section.skyLight.isNotEmpty()) setByteArray("SkyLight", section.skyLight)
            })
    }
    data["Sections"] = sectionList

    val heightmapData = NBTCompound()
    heightmaps.forEach {
        if (it.key in Heightmap.Type.POST_FEATURES) heightmapData.setLongArray(it.key.name, it.value.data.data)
    }
    data["Heightmaps"] = heightmapData
    return NBTCompound()
        .setInt("DataVersion", CHUNK_DATA_VERSION)
        .set("Level", data)
}

const val CHUNK_DATA_VERSION = 2578
