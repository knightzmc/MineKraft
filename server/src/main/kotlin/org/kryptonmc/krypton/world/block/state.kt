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

import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTString
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.api.util.toKey
import org.kryptonmc.krypton.util.IdMapper
import org.kryptonmc.krypton.world.transform

val BLOCKS = IdMapper<Block>().apply {
    KryptonBlockLoader.STATE_MAP.forEach { set(it.value, it.key) }
}

fun NBTCompound.toBlock(): Block {
    if (!contains("Name", NBTTypes.TAG_String)) return Blocks.AIR
    var block = BLOCK_LOADER.fromKey(getString("Name").toKey()) ?: error("No block found with key ${getString("Name")}!")
    if (contains("Properties", NBTTypes.TAG_Compound)) {
        block = block.withProperties(getCompound("Properties").transform { it.key to (it.value as NBTString).value })
    }
    return block
}

fun Block.toNBT() = NBTCompound()
    .setString("Name", key.asString())
    .apply {
        if (properties.isNotEmpty()) {
            val propertiesTag = NBTCompound().apply { properties.forEach { setString(it.key, it.value) } }
            set("Properties", propertiesTag)
        }
    }
