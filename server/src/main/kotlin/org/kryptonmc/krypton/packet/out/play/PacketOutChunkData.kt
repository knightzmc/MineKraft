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
package org.kryptonmc.krypton.packet.out.play

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.kryptonmc.krypton.packet.state.PlayPacket
import org.kryptonmc.krypton.util.writeBitSet
import org.kryptonmc.krypton.util.writeNBTCompound
import org.kryptonmc.krypton.util.writeVarInt
import org.kryptonmc.krypton.world.chunk.KryptonChunk
import java.util.BitSet

/**
 * This packet is very strange and really weird to compute, so don't expect to understand it straight away.
 *
 * I recommend reading [wiki.vg](https://wiki.vg/Chunk_Format) for more information on this, as they can do
 * a much better job at explaining it than I can.
 *
 * @param chunk the chunk to send the data of
 */
class PacketOutChunkData(private val chunk: KryptonChunk) : PlayPacket(0x22) {

    private val buffer = ByteArray(chunk.calculateSize())
    private val sectionMask = chunk.extract(Unpooled.wrappedBuffer(buffer).apply { writerIndex(0) })

    override fun write(buf: ByteBuf) {
        buf.writeInt(chunk.position.x)
        buf.writeInt(chunk.position.z)

        // Mask
        buf.writeBitSet(sectionMask)

        // Heightmaps
        val heightmaps = NBTCompound()
        chunk.heightmaps.forEach { if (it.key.sendToClient) heightmaps.setLongArray(it.key.name, it.value.data.data) }
        buf.writeNBTCompound(heightmaps)

        buf.writeVarInt(chunk.biomes.size)
        chunk.biomes.forEach { buf.writeVarInt(it.id) }

        // Actual chunk data
        buf.writeVarInt(buffer.size)
        buf.writeBytes(buffer)

        // TODO: When block entities are added, make use of this here
        buf.writeVarInt(0) // number of block entities
    }

    private fun KryptonChunk.extract(buf: ByteBuf): BitSet {
        val mask = BitSet()
        for (i in sections.indices) {
            val section = sections[i]
            if (section != null && !section.isEmpty()) {
                mask.set(i)
                section.write(buf)
            }
        }
        return mask
    }

    private fun KryptonChunk.calculateSize(): Int {
        var size = 0
        for (i in sections.indices) {
            val section = sections[i]
            if (section != null && !section.isEmpty()) size += section.serializedSize
        }
        return size
    }

    companion object {

        // Thanks Minestom :) (https://github.com/Minestom/Minestom/blob/master/src/main/java/net/minestom/server/network/packet/server/play/ChunkDataPacket.java#L50-52)
        private const val CHUNK_SECTION_COUNT = 16
        private const val MAX_BITS_PER_ENTRY = 16
        private const val MAX_BUFFER_SIZE = (Short.SIZE_BYTES + Byte.SIZE_BYTES + 5 * Byte.SIZE_BYTES + 4096 * MAX_BITS_PER_ENTRY / Long.SIZE_BITS * Long.SIZE_BYTES) * CHUNK_SECTION_COUNT + 256 * Int.SIZE_BYTES
    }
}
