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
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTReader
import org.jglrxavpok.hephaistos.nbt.NBTWriter
import org.kryptonmc.api.effect.particle.ColorParticleData
import org.kryptonmc.api.effect.particle.DirectionalParticleData
import org.kryptonmc.api.effect.particle.NoteParticleData
import org.kryptonmc.api.effect.particle.ParticleEffect
import org.kryptonmc.api.inventory.item.ItemStack
import org.kryptonmc.api.space.Position
import org.kryptonmc.api.space.Vector
import org.kryptonmc.api.world.Location
import org.kryptonmc.krypton.entity.data.VillagerData
import org.kryptonmc.krypton.locale.TranslationManager
import org.kryptonmc.krypton.registry.FileRegistries
import org.spongepowered.math.vector.Vector3i
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration
import java.util.BitSet
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.ceil
import kotlin.math.min

// Allows us to write a byte without having to convert it to an integer every time
fun ByteBuf.writeByte(byte: Byte) {
    writeByte(byte.toInt())
}

fun ByteBuf.writeUByte(byte: UByte) {
    writeByte(byte.toInt())
}

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
        else -> writeVarIntFull(value)
    }
}

// This came from Velocity. See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
fun ByteBuf.write21BitVarInt(value: Int) {
    writeMedium(value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14))
}

// This came from Velocity. See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
private fun ByteBuf.writeVarIntFull(value: Int) {
    when {
        value.toLong() and (0xFFFFFFFF shl 21) == 0L -> writeMedium(value and 0x7F or 0x80 shl 16 or (value ushr 7 and 0x7F or 0x80 shl 8) or (value ushr 14))
        value.toLong() and (0xFFFFFFFF shl 28) == 0L -> writeInt(value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value ushr 14 and 0x7F or 0x80 shl 8) or (value ushr 21))
        else -> {
            writeInt(value and 0x7F or 0x80 shl 24 or (value ushr 7 and 0x7F or 0x80 shl 16) or (value shr 14 and 0x7F or 0x80 shl 8) or (value ushr 21 and 0x7F or 0x80))
            writeByte(value ushr 28)
        }
    }
}

fun ByteBuf.writeVarLong(varLong: Long) {
    var i = varLong
    while (i and -128 != 0L) {
        writeByte((i and 127 or 128).toInt())
        i = i ushr 7
    }

    writeByte(i.toInt())
}

fun ByteBuf.readString(maxLength: Short = Short.MAX_VALUE): String {
    val length = readVarInt()

    return when {
        length > maxLength * 4 ->
            throw IOException("The received encoded string buffer length is longer than maximum allowed (" + length + " > " + maxLength * 4 + ")")
        length < 0 ->
            throw IOException("The received encoded string buffer length is less than zero! Weird string!")
        else -> {
            val array = ByteArray(length)
            readBytes(array)
            val s = String(array, UTF_8)
            if (s.length > maxLength) {
                throw IOException("The received string length is longer than maximum allowed ($length > $maxLength)")
            } else {
                s
            }
        }
    }
}

fun ByteBuf.writeString(string: String, maxLength: Short = Short.MAX_VALUE) {
    val bytes = string.toByteArray(Charsets.UTF_8)
    if (bytes.size > maxLength) {
        throw EncoderException("String too big (was " + bytes.size + " bytes encoded, max " + maxLength + ")")
    } else {
        writeVarInt(bytes.size)
        writeBytes(bytes)
    }
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

fun ByteBuf.writeByteArray(array: ByteArray) {
    writeVarInt(array.size)
    writeBytes(array)
}

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
    writeString(GsonComponentSerializer.gson().serialize(TranslationManager.render(component)))
}

fun ByteBuf.writeItem(item: ItemStack?, nbt: NBTCompound?) {
    if (item == null) {
        writeBoolean(false)
        return
    }
    writeBoolean(true)
    writeVarInt(FileRegistries.ITEMS.idOf(item.type.key()))
    writeByte(item.amount)
    nbt?.let { writeNBTCompound(it) } ?: writeByte(0)
}

fun ByteBuf.writeRotation(rotation: Rotation) {
    writeFloat(rotation.x)
    writeFloat(rotation.y)
    writeFloat(rotation.z)
}

fun ByteBuf.writePosition(position: Position) {
    writeLong(position.toProtocol())
}

fun ByteBuf.writeVector(vector: Vector3i) {
    writeLong(vector.x().toLong() and 0x3FFFFFF shl 38 or (vector.z().toLong() and 0x3FFFFFF shl 12) or (vector.y().toLong() and 0xFFF))
}

fun ByteBuf.writeParticle(particle: ParticleEffect, location: Location) {
    writeInt(org.kryptonmc.api.registry.Registries.PARTICLE_TYPE.idOf(particle.type))
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
    writeUByte(angle.value)
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

fun <T> ByteBuf.writeOptional(optional: java.util.Optional<T>, presentAction: (T) -> Unit) {
    writeBoolean(optional.isPresent)
    if (optional.isPresent) presentAction(optional.get())
}

fun <E> ByteBuf.writeCollection(collection: Collection<E>, action: (E) -> Unit) {
    writeVarInt(collection.size)
    collection.forEach { action(it) }
}

fun Int.varIntSize(): Int {
    for (i in 1 until 5) {
        if (this and (-1 shl i * 7) != 0) continue
        return i
    }
    return 5
}

private val VARINT_EXACT_BYTE_LENGTHS = IntArray(33) { ceil((31.0 - (it - 1)) / 7.0).toInt() }.apply { this[32] = 1 }

val Int.varIntBytes: Int
    get() = VARINT_EXACT_BYTE_LENGTHS[countLeadingZeroBits()]
