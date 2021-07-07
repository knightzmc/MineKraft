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
import org.kryptonmc.krypton.packet.state.PlayPacket
import org.kryptonmc.krypton.util.writeBitSet
import org.kryptonmc.krypton.util.writeByteArray
import org.kryptonmc.krypton.util.writeCollection
import org.kryptonmc.krypton.util.writeVarInt
import org.kryptonmc.krypton.world.chunk.KryptonChunk
import org.kryptonmc.krypton.world.light.LightLayer
import java.util.BitSet

/**
 * Update the light levels for the specified [chunk]
 *
 * @param chunk the chunk to update the light levels for
 */
// TODO: Fix light when we add the new engine
class PacketOutUpdateLight(
    private val chunk: KryptonChunk,
    private val skyChangedFilter: BitSet? = null,
    private val blockChangedFilter: BitSet? = null,
    private val trustEdges: Boolean = true
) : PlayPacket(0x25) {

    private val x = chunk.position.x
    private val z = chunk.position.z
    private val lightEngine = chunk.world.chunkManager.lightEngine

    override fun write(buf: ByteBuf) {
        val skyMask = BitSet()
        val emptySkyMask = BitSet()
        val blockMask = BitSet()
        val emptyBlockMask = BitSet()
        val skyUpdates = mutableListOf<ByteArray>()
        val blockUpdates = mutableListOf<ByteArray>()

        for (i in 0 until lightEngine.lightSectionCount) {
            if (skyChangedFilter == null || skyChangedFilter[i]) chunk.prepareData(x, z, LightLayer.SKY, i, skyMask, emptySkyMask, skyUpdates)
            if (blockChangedFilter == null || blockChangedFilter[i]) chunk.prepareData(x, z, LightLayer.BLOCK, i, blockMask, emptyBlockMask, blockUpdates)
        }

        buf.writeVarInt(x)
        buf.writeVarInt(z)
        buf.writeBoolean(trustEdges)
        buf.writeBitSet(skyMask)
        buf.writeBitSet(blockMask)
        buf.writeBitSet(emptySkyMask)
        buf.writeBitSet(emptyBlockMask)
        buf.writeCollection(skyUpdates) { buf.writeByteArray(it) }
        buf.writeCollection(blockUpdates) { buf.writeByteArray(it) }
    }

    private fun KryptonChunk.prepareData(x: Int, z: Int, layer: LightLayer, index: Int, mask: BitSet, emptyMask: BitSet, updates: MutableList<ByteArray>) {
        val data = when (layer) {
            LightLayer.SKY -> lightEngine.getSkyData(x, lightEngine.minLightSection + index, z)
            LightLayer.BLOCK -> lightEngine.getBlockData(x, lightEngine.minLightSection + index, z)
        }
        if (data != null && !data.isHiddenVisible && !data.isNullNibbleVisible) {
            if (data.isUninitialisedVisible) {
                emptyMask.set(index)
            } else {
                mask.set(index)
                updates.add(data.storageVisible)
            }
        }
        if (data == null) println("Light data for layer $layer in chunk $x, $z at y $index was null!")
    }
}
