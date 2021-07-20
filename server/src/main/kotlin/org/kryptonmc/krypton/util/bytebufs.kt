/*
 * This file is part of the Krypton project, and parts of it originate from the
 * Velocity project, licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 * Copyright (C) 2018 Velocity Contributors
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
 *
 * For the original file that parts of this file are derived from, see here:
 * https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java
 */
package org.kryptonmc.krypton.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.ints.IntList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTReader
import org.jglrxavpok.hephaistos.nbt.NBTWriter
import org.kryptonmc.api.adventure.toJsonString
import org.kryptonmc.api.block.BlockHitResult
import org.kryptonmc.api.effect.particle.ColorParticleData
import org.kryptonmc.api.effect.particle.DirectionalParticleData
import org.kryptonmc.api.effect.particle.NoteParticleData
import org.kryptonmc.api.effect.particle.ParticleEffect
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.api.space.Direction
import org.kryptonmc.api.space.Position
import org.kryptonmc.api.space.Vector
import org.kryptonmc.api.util.toVector
import org.kryptonmc.api.world.Location
import org.kryptonmc.krypton.entity.data.VillagerData
import org.kryptonmc.krypton.item.EmptyItemStack
import org.kryptonmc.krypton.item.KryptonItemStack
import org.kryptonmc.krypton.item.meta.KryptonMetaHolder
import org.kryptonmc.krypton.locale.TranslationManager
import org.spongepowered.math.vector.Vector3d
import org.spongepowered.math.vector.Vector3i
import java.io.IOException
import java.time.Duration
import java.util.BitSet
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil
import kotlin.math.min

fun ByteBuf.writeShort(short: Short) {
    writeShort(short.toInt())
}

fun ByteBuf.readVarInt(): Int {
    var i = 0
    val maxRead = min(5, readableBytes())
    for (j in 0 until maxRead) {
        val k = readByte()
        i = i or (k.toInt() and 0x7F shl j * 7)
        if (k.toInt() and 0x80 != 128) return i
    }
    return Int.MAX_VALUE
}

// This came from Velocity. See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
fun ByteBuf.writeVarInt(value: Int) {
    when {
        value.toLong() and (0xFFFFFFFF shl 7) == 0L -> writeByte(value)
        value.toLong() and (0xFFFFFFFF shl 14) == 0L -> writeShort(value and 0x7F or 0x80 shl 8 or (value ushr 7))
        value.toLong() and (0xFFFFFFFF shl 21) == 0L -> writeMedium(value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14))
        value.toLong() and (0xFFFFFFFF shl 28) == 0L -> writeInt(value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21))
        else -> {
            writeInt(value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value shr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80))
            writeByte(value ushr 28)
        }
    }
}

fun ByteBuf.writeVarLong(value: Long) {
    when {
        value and (0xFFFFFFFF shl 7) == 0L -> writeByte(value.toInt())
        value and (0xFFFFFFFF shl 14) == 0L -> writeShort((value and 0x7F or 0x80 shl 8 or (value ushr 7)).toInt())
        value and (0xFFFFFFFF shl 21) == 0L -> writeMedium((value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14)).toInt())
        value and (0xFFFFFFFF shl 28) == 0L -> writeInt((value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21)).toInt())
        value and (0xFFFFFFFF shl 35) == 0L -> {
            writeInt((value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80)).toInt())
            writeByte((value ushr 28).toInt())
        }
        value and (0xFFFFFFFF shl 42) == 0L -> {
            writeInt((value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80)).toInt())
            writeShort((value ushr 28 and 0x7F or 0x80 shl 8 or (value ushr 35)).toInt())
        }
        value and (0xFFFFFFFF shl 49) == 0L -> {
            writeInt((value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80)).toInt())
            writeMedium((value ushr 28 and 0x7F or 0x80 shl 16 or (value ushr 35 and 0x7F or 0x80 shl 8) or (value ushr 42)).toInt())
        }
        value and (0xFFFFFFFF shl 56) == 0L -> writeLong((value and 0x7F or 0x80 shl 56 or (value ushr 7 and 0x7F or 0x80 shl 48) or (value ushr 14 and 0x7F or 0x80 shl 40) or (value ushr 21 and 0x7F or 0x80 shl 32) or (value ushr 28 and 0x7F or 0x80 shl 24) or (value ushr 35 and 0x7F or 0x80 shl 16) or (value ushr 42 and 0x7F or 0x80 shl 8) or (value ushr 49)))
        value and (0xFFFFFFFF shl 63) == 0L -> {
            writeLong((value and 0x7F or 0x80 shl 56 or (value ushr 7 and 0x7F or 0x80 shl 48) or (value ushr 14 and 0x7F or 0x80 shl 40) or (value ushr 21 and 0x7F or 0x80 shl 32) or (value ushr 28 and 0x7F or 0x80 shl 24) or (value ushr 35 and 0x7F or 0x80 shl 16) or (value ushr 42 and 0x7F or 0x80 shl 8) or (value ushr 49 and 0x7F or 0x80)))
            writeByte((value ushr 56).toInt())
        }
        value and (0xFFFFFFFF shl 70) == 0L -> {
            writeLong((value and 0x7F or 0x80 shl 56 or (value ushr 7 and 0x7F or 0x80 shl 48) or (value ushr 14 and 0x7F or 0x80 shl 40) or (value ushr 21 and 0x7F or 0x80 shl 32) or (value ushr 28 and 0x7F or 0x80 shl 24) or (value ushr 35 and 0x7F or 0x80 shl 16) or (value ushr 42 and 0x7F or 0x80 shl 8) or (value ushr 49 and 0x7F or 0x80)))
            writeShort((value ushr 56 and 0x7F or 0x80 shl 8 or (value ushr 63)).toInt())
        }
    }
}

fun ByteBuf.readString(max: Short = Short.MAX_VALUE): String {
    val length = readVarInt()
    return when {
        length > max * 4 -> throw IOException("String too long! Expected maximum length of $max, got length of $length!")
        length < 0 -> throw IOException("String cannot be less than 0 in length!")
        else -> {
            val string = String(readAvailableBytes(length))
            if (string.length > max) throw IOException("String too long! Expected maximum length of $max, got length of ${string.length}") else string
        }
    }
}

fun ByteBuf.writeString(value: String, max: Short = Short.MAX_VALUE) {
    val bytes = value.encodeToByteArray().takeIf { it.size <= max } ?: throw EncoderException("String too long! Expected maximum size of $max, got length ${value.length}!")
    writeVarInt(bytes.size)
    writeBytes(bytes)
}

fun ByteBuf.readVarIntByteArray(): ByteArray {
    val length = readVarInt()
    val readable = readableBytes()
    if (length > readable) {
        throw DecoderException("Not enough bytes to read. Expected Length: $length, actual length: $readable")
    }
    return readAvailableBytes(length)
}

fun ByteBuf.readAvailableBytes(length: Int): ByteArray {
    val bytes = ByteArray(length)
    readBytes(bytes, 0, length)
    return bytes
}

fun ByteBuf.readAllAvailableBytes() = readAvailableBytes(readableBytes())

fun ByteBuf.writeLongArray(array: LongArray) {
    writeVarInt(array.size)
    array.forEach { writeLong(it) }
}

fun ByteBuf.writeUUID(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

fun ByteBuf.writeNBTCompound(tag: NBTCompound) {
    val outputStream = ByteBufOutputStream(this)
    NBTWriter(outputStream, false).use { it.writeNamed("", tag) }
}

fun ByteBuf.readNBTCompound(): NBTCompound {
    val index = readerIndex()
    val type = readByte()
    if (type == 0.toByte()) return NBTCompound()
    readerIndex(index) // reset the head if it's not an end tag

    try {
        return NBTReader(ByteBufInputStream(this), false).use { it.read() as NBTCompound }
    } catch (exception: IOException) {
        throw DecoderException(exception)
    }
}

fun ByteBuf.writeChat(component: Component) {
    writeString(TranslationManager.render(component).toJsonString())
}

fun ByteBuf.readItem(): KryptonItemStack {
    if (!readBoolean()) return EmptyItemStack
    val id = readVarInt()
    val count = readByte()
    val nbt = readNBTCompound()
    return KryptonItemStack(Registries.ITEM[id]!!, count.toInt(), KryptonMetaHolder(nbt))
}

fun ByteBuf.writeItem(item: KryptonItemStack) {
    if (item === EmptyItemStack) {
        writeBoolean(false)
        return
    }
    writeBoolean(true)
    writeVarInt(Registries.ITEM.idOf(item.type))
    writeByte(item.amount)
    writeNBTCompound(item.meta.nbt)
}

fun ByteBuf.writeRotation(rotation: Rotation) {
    writeFloat(rotation.x)
    writeFloat(rotation.y)
    writeFloat(rotation.z)
}

fun ByteBuf.writePosition(position: Position) {
    writeLong(position.toProtocol())
}

fun ByteBuf.readVector() = readLong().toVector()

fun ByteBuf.writeVector(vector: Vector3i) {
    writeVector(vector.x(), vector.y(), vector.z())
}

fun ByteBuf.writeVector(x: Int, y: Int, z: Int) {
    writeLong(x.toLong() and 0x3FFFFFF shl 38 or (z.toLong() and 0x3FFFFFF shl 12) or (y.toLong() and 0xFFF))
}

fun ByteBuf.writeParticle(particle: ParticleEffect, location: Location) {
    writeInt(Registries.PARTICLE_TYPE.idOf(particle.type))
    writeBoolean(particle.longDistance)

    val data = particle.data

    // Write location. If the particle is directional, colorable, or a note then we need to manually apply the offsets first
    when (data) {
        is DirectionalParticleData, is ColorParticleData, is NoteParticleData -> {
            val random = ThreadLocalRandom.current()
            writeDouble(location.x + particle.offset.x * random.nextGaussian())
            writeDouble(location.y + particle.offset.y * random.nextGaussian())
            writeDouble(location.z + particle.offset.z * random.nextGaussian())
        }
        else -> {
            writeDouble(location.x)
            writeDouble(location.y)
            writeDouble(location.z)
        }
    }

    // Write offsets depending on what type of particle it is
    when (data) {
        // Particle is directional, the offset fields are used for the direction
        is DirectionalParticleData -> {
            val random = ThreadLocalRandom.current()
            val direction = data.direction ?: Vector(random.nextGaussian(), random.nextGaussian(), random.nextGaussian())

            writeFloat(direction.x.toFloat())
            writeFloat(direction.y.toFloat())
            writeFloat(direction.z.toFloat())
        }
        // Particle is colorable, the offset fields are used to define the color
        is ColorParticleData -> {
            writeFloat(data.red.toFloat() / 255F)
            writeFloat(data.green.toFloat() / 255F)
            writeFloat(data.blue.toFloat() / 255F)
        }
        // Particle is a note, the offset fields are used to define the note value (only the x field)
        is NoteParticleData -> {
            writeFloat(data.note.toFloat() / 24F)
            writeFloat(0.0F)
            writeFloat(0.0F)
        }
        // Particle is normal, let the client handle the offsets
        else -> {
            writeFloat(particle.offset.x.toFloat())
            writeFloat(particle.offset.y.toFloat())
            writeFloat(particle.offset.z.toFloat())
        }
    }

    // Write the extra data
    when (data) {
        // Used for the velocity
        is DirectionalParticleData -> writeFloat(data.velocity)
        // Default to 1 otherwise
        else -> writeFloat(1.0F)
    }

    // Write the count
    when (data) {
        // Count needs to be set to 0 so the special particle properties get applied
        is DirectionalParticleData, is ColorParticleData, is NoteParticleData -> writeInt(0)
        // Count is set to whatever is defined
        else -> writeInt(particle.quantity)
    }

    // Write the data, if applicable
    particle.write(this)
}

fun ByteBuf.writeVillagerData(data: VillagerData) {
    writeVarInt(data.type.id)
    writeVarInt(data.profession.id)
    writeVarInt(data.level)
}

fun ByteBuf.writeAngle(angle: Angle) {
    writeByte(angle.value.toInt())
}

fun ByteBuf.writeKey(key: Key) {
    writeString(key.asString())
}

fun ByteBuf.writeDuration(duration: Duration) {
    writeInt(duration.seconds.toInt() * 20)
}

inline fun <reified T : Enum<T>> ByteBuf.readEnum(): T = T::class.java.enumConstants[readVarInt()]

fun ByteBuf.writeEnum(enum: Enum<*>) = writeVarInt(enum.ordinal)

fun ByteBuf.writeBitSet(set: BitSet) = writeLongArray(set.toLongArray())

fun <T> ByteBuf.writeOptional(optional: Optional<T>, presentAction: (T) -> Unit) {
    writeBoolean(optional.isPresent)
    if (optional.isPresent) presentAction(optional.get())
}

fun <E> ByteBuf.writeCollection(collection: Collection<E>, action: (E) -> Unit) {
    writeVarInt(collection.size)
    collection.forEach { action(it) }
}

fun ByteBuf.writeIntList(list: IntList) {
    writeVarInt(list.size)
    list.forEach { writeVarInt(it) }
}

fun ByteBuf.readBlockHitResult(): BlockHitResult {
    val position = readVector()
    val direction = readEnum<Direction>()
    val cursorX = readFloat()
    val cursorY = readFloat()
    val cursorZ = readFloat()
    val inside = readBoolean()
    val clickedPosition = Vector3d(position.x() + cursorX, position.y() + cursorY, position.z() + cursorZ)
    return BlockHitResult(clickedPosition, position, direction, false, inside)
}

private val VARINT_EXACT_BYTE_LENGTHS = IntArray(33) { ceil((31.0 - (it - 1)) / 7.0).toInt() }.apply { this[32] = 1 }

val Int.varIntBytes: Int
    get() = VARINT_EXACT_BYTE_LENGTHS[countLeadingZeroBits()]
