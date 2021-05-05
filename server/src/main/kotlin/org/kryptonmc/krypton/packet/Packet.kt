/*
 * This file is part of the Krypton project, licensed under the GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kryptonmc.krypton.packet

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.packet.state.PacketState

/**
 * Super interface for all inbound and outbound packets.
 */
interface Packet {

    /**
     * This packet's info
     */
    val info: PacketInfo

    /**
     * Read this packet's data from the given [buf]
     */
    fun read(buf: ByteBuf) {
        throw UnsupportedOperationException("$javaClass does not support reading")
    }

    /**
     * Write this packet's data to the given [buf]
     */
    fun write(buf: ByteBuf) {
        throw UnsupportedOperationException("$javaClass does not support writing")
    }
}

/**
 * Holder for packet information
 */
data class PacketInfo(val id: Int, val state: PacketState)
