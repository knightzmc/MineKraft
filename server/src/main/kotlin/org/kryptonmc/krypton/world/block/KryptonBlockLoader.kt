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

import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import me.bardy.gsonkt.fromJson
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.internal.BlockLoader
import org.kryptonmc.krypton.GSON
import org.kryptonmc.krypton.registry.block.BlockData
import java.util.concurrent.ConcurrentHashMap

class KryptonBlockLoader : BlockLoader {

    override fun fromKey(key: String): Block? {
        val id = if (key.indexOf(':') == -1) "minecraft:$key" else key
        return KEY_MAP[id]
    }

    override fun fromKey(key: Key) = fromKey(key.asString())

    companion object {

        private val KEY_MAP = mutableMapOf<String, Block>()
        private val PROPERTY_MAP = mutableMapOf<String, PropertyEntry>()
        private val ID_MAP = Int2ObjectOpenHashMap<Block>()
        val STATE_MAP = Int2ObjectOpenHashMap<Block>()

        init {
            val blocks = GSON.fromJson<JsonObject>(Thread.currentThread().contextClassLoader.getResourceAsStream("1_17_blocks.json")!!.reader(Charsets.UTF_8))
            blocks.entrySet().asSequence().map { it.key to it.value.asJsonObject }.forEach { (key, value) ->
                val propertyEntry = PropertyEntry()
                value.remove("states").asJsonArray.forEach {
                    val (properties, block) = it.asJsonObject.retrieveState(key, value)
                    propertyEntry.properties[properties] = block
                }
                val defaultState = value["defaultStateId"].asInt
                val defaultBlock = fromState(defaultState)!!
                val id = value["id"].asInt
                ID_MAP[id] = defaultBlock
                KEY_MAP[key] = defaultBlock
                PROPERTY_MAP[key] = propertyEntry
            }
        }

        fun fromId(id: Int): Block? = ID_MAP[id]

        private fun fromState(stateId: Int): Block? = STATE_MAP[stateId]

        fun properties(key: String, properties: Map<String, String>): Block? = PROPERTY_MAP[key]?.properties?.get(properties)

        private fun JsonObject.retrieveState(key: String, blockObject: JsonObject): Pair<Map<String, String>, Block> {
            val stateId = get("stateId").asInt
            val propertyMap = get("properties").asJsonObject.entrySet().associate { it.key to it.value.asString.lowercase() }
            val block = KryptonBlock(BlockData(Key.key(key), blockObject, this), propertyMap)
            STATE_MAP[stateId] = block
            return propertyMap to block
        }
    }
}

val BLOCK_LOADER = Services.service(BlockLoader::class.java).get()

private class PropertyEntry {

    val properties = ConcurrentHashMap<Map<String, String>, Block>()
}

private fun registryData(path: String) = (Thread.currentThread().contextClassLoader.getResourceAsStream(path)
    ?: error("$path not on classpath! Something has gone horribly wrong!"))
    .reader()
    .readText()
