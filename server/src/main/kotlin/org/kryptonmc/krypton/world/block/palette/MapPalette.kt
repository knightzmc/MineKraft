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
package org.kryptonmc.krypton.world.block.palette

import io.netty.buffer.ByteBuf
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.kryptonmc.api.block.Block
import org.kryptonmc.krypton.util.IntIdentityHashBiMap
import org.kryptonmc.krypton.util.varIntSize
import org.kryptonmc.krypton.util.writeVarInt
import org.kryptonmc.krypton.world.block.BLOCKS
import org.kryptonmc.krypton.world.block.toBlock
import org.kryptonmc.krypton.world.block.toNBT

class MapPalette(private val bits: Int, private val resizer: (Int, Block) -> Int) : Palette {

    private val values = IntIdentityHashBiMap(Block::class.java, 1 shl bits)

    override fun get(value: Block): Int {
        var id = values.idOf(value)
        if (id == -1) {
            id = values.add(value)
            if (id >= 1 shl bits) id = resizer(bits + 1, value)
        }
        return id
    }

    override fun get(id: Int) = values[id]

    override fun write(buf: ByteBuf) {
        buf.writeVarInt(size)
        for (i in 0 until size) buf.writeVarInt(BLOCKS.idOf(values[i]!!))
    }

    override fun load(tag: NBTList<NBTCompound>) {
        values.clear()
        for (i in tag.indices) values.add(tag[i].toBlock())
    }

    fun save() = NBTList<NBTCompound>(NBTTypes.TAG_Compound).apply {
        for (i in indices) add(values[i]!!.toNBT())
    }

    override val size: Int
        get() = values.size

    override val serializedSize: Int
        get() {
            var temp = size.varIntSize()
            for (i in 0 until size) temp += BLOCKS.idOf(values[i]!!).varIntSize()
            return temp
        }
}
