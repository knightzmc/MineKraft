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
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.block.Blocks
import org.kryptonmc.krypton.util.varIntSize
import org.kryptonmc.krypton.world.block.BLOCKS

object GlobalPalette : Palette {

    override val size = BLOCKS.size
    override val serializedSize = 0.varIntSize()

    override fun get(value: Block) = BLOCKS.idOf(value).takeIf { it != -1 } ?: 0

    override fun get(id: Int) = BLOCKS[id] ?: Blocks.AIR

    override fun write(buf: ByteBuf) = Unit

    override fun load(tag: NBTList<NBTCompound>) = Unit
}
