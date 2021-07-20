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
package org.kryptonmc.krypton.util.nbt

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTByte
import org.jglrxavpok.hephaistos.nbt.NBTByteArray
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTDouble
import org.jglrxavpok.hephaistos.nbt.NBTFloat
import org.jglrxavpok.hephaistos.nbt.NBTInt
import org.jglrxavpok.hephaistos.nbt.NBTIntArray
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTLong
import org.jglrxavpok.hephaistos.nbt.NBTLongArray
import org.jglrxavpok.hephaistos.nbt.NBTNumber
import org.jglrxavpok.hephaistos.nbt.NBTShort
import org.jglrxavpok.hephaistos.nbt.NBTString
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.kryptonmc.api.adventure.toMessage

class BrigadierSNBTParser(private val reader: StringReader) {

    private fun readKey(): String {
        reader.skipWhitespace()
        return if (!reader.canRead()) throw ERROR_EXPECTED_KEY.createWithContext(reader) else reader.readString()
    }

    fun readValue(): NBT {
        reader.skipWhitespace()
        if (!reader.canRead()) throw ERROR_EXPECTED_VALUE.createWithContext(reader)
        return when (reader.peek()) {
            COMPOUND_START -> readCompound()
            LIST_START -> readList()
            else -> readTypedValue()
        }
    }

    private fun readTypedValue(): NBT {
        reader.skipWhitespace()
        val cursor = reader.cursor
        if (reader.peek().isQuotedStringStart) return NBTString(reader.readQuotedString())
        val text = reader.readUnquotedString()
        if (text.isEmpty()) {
            reader.cursor = cursor
            throw ERROR_EXPECTED_VALUE.createWithContext(reader)
        }
        return convert(text)
    }

    private fun readList() = if (reader.canRead(3) && !reader.peek(1).isQuotedStringStart && reader.peek(2) == ';') readArrayTag() else readListTag()

    private fun readListTag(): NBT {
        expect(LIST_START)
        reader.skipWhitespace()
        if (!reader.canRead()) throw ERROR_EXPECTED_VALUE.createWithContext(reader)
        val list = NBTList<NBT>(NBTTypes.TAG_End)
        var type: Int = -1

        while (reader.peek() != LIST_END) {
            val cursor = reader.cursor
            val tag = readValue()
            val tagType = tag.ID
            if (type == -1) {
                type = tagType
            } else if (tagType != type) {
                reader.cursor = cursor
                throw ERROR_INSERT_MIXED_LIST.createWithContext(reader, NBTTypes.name(tagType), NBTTypes.name(type))
            }
            list += tag
            if (!reader.hasElementSeparator) break
            if (!reader.canRead()) throw ERROR_EXPECTED_VALUE.createWithContext(reader)
        }

        expect(LIST_END)
        return list
    }

    private fun readArrayTag(): NBT {
        expect(LIST_START)
        val cursor = reader.cursor
        val start = reader.read()
        reader.read()
        reader.skipWhitespace()
        if (!reader.canRead()) throw ERROR_EXPECTED_VALUE.createWithContext(reader)
        return when (start) {
            BYTE_ARRAY_START -> NBTByteArray(readArray<Byte>(NBTTypes.TAG_Byte_Array, NBTTypes.TAG_Byte).toByteArray())
            INT_ARRAY_START -> NBTIntArray(readArray<Int>(NBTTypes.TAG_Int_Array, NBTTypes.TAG_Int).toIntArray())
            LONG_ARRAY_START -> NBTLongArray(readArray<Long>(NBTTypes.TAG_Long_Array, NBTTypes.TAG_Long).toLongArray())
            else -> {
                reader.cursor = cursor
                throw ERROR_INVALID_ARRAY.createWithContext(reader, start.toString())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Number> readArray(arrayType: Int, elementType: Int): List<T> {
        val list = mutableListOf<T>()
        while (true) {
            if (reader.peek() != LIST_END) {
                val cursor = reader.cursor
                val tag = readValue()
                val type = tag.ID
                if (type != elementType) {
                    reader.cursor = cursor
                    throw ERROR_INSERT_MIXED_ARRAY.createWithContext(reader, NBTTypes.name(type), NBTTypes.name(arrayType))
                }
                list += when (elementType) {
                    NBTTypes.TAG_Byte -> (tag as NBTNumber<Byte>).value as T
                    NBTTypes.TAG_Long -> (tag as NBTNumber<Long>).value as T
                    else -> (tag as NBTNumber<Int>).value as T
                }
                if (reader.hasElementSeparator) {
                    if (!reader.canRead()) throw ERROR_EXPECTED_VALUE.createWithContext(reader)
                    continue
                }
            }

            expect(LIST_END)
            return list
        }
    }

    fun readCompound(): NBTCompound {
        expect(COMPOUND_START)
        val tag = NBTCompound()
        reader.skipWhitespace()

        while (reader.canRead() && reader.peek() != COMPOUND_END) {
            val cursor = reader.cursor
            val key = readKey()
            if (key.isEmpty()) {
                reader.cursor = cursor
                throw ERROR_EXPECTED_KEY.createWithContext(reader)
            }
            expect(KEY_SEPARATOR)
            tag[key] = readValue()
            if (!reader.hasElementSeparator) break
            if (!reader.canRead()) throw ERROR_EXPECTED_KEY.createWithContext(reader)
        }
        expect(COMPOUND_END)
        return tag
    }

    private fun convert(text: String): NBT = when {
        text matches BYTE_REGEX -> NBTByte(text.dropLast(1).toByte())
        text matches SHORT_REGEX -> NBTShort(text.dropLast(1).toShort())
        text matches INT_REGEX -> NBTInt(text.toInt())
        text matches LONG_REGEX -> NBTLong(text.dropLast(1).toLong())
        text matches FLOAT_REGEX -> NBTFloat(text.dropLast(1).toFloat())
        text matches DOUBLE_REGEX -> NBTDouble(text.dropLast(1).toDouble())
        text matches DOUBLE_REGEX_NO_SUFFIX -> NBTDouble(text.toDouble())
        text == "true" -> NBTByte(1)
        text == "false" -> NBTByte(0)
        else -> NBTString(text)
    }

    private fun expect(char: Char) {
        reader.skipWhitespace()
        reader.expect(char)
    }

    companion object {

        private const val ELEMENT_SEPARATOR = ','
        private const val KEY_SEPARATOR = ':'
        private const val LIST_START = '['
        private const val LIST_END = ']'
        private const val COMPOUND_START = '{'
        private const val COMPOUND_END = '}'
        private const val BYTE_ARRAY_START = 'B'
        private const val LONG_ARRAY_START = 'L'
        private const val INT_ARRAY_START = 'I'

        private val DOUBLE_REGEX_NO_SUFFIX = "[-+]?(?:[0-9]+[.]|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?".toRegex(RegexOption.IGNORE_CASE)
        private val DOUBLE_REGEX = "[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?d".toRegex(RegexOption.IGNORE_CASE)
        private val FLOAT_REGEX = "[-+]?(?:[0-9]+[.]?|[0-9]*[.][0-9]+)(?:e[-+]?[0-9]+)?f".toRegex(RegexOption.IGNORE_CASE)
        private val BYTE_REGEX = "[-+]?(?:0|[1-9][0-9]*)b".toRegex(RegexOption.IGNORE_CASE)
        private val LONG_REGEX = "[-+]?(?:0|[1-9][0-9]*)l".toRegex(RegexOption.IGNORE_CASE)
        private val SHORT_REGEX = "[-+]?(?:0|[1-9][0-9]*)s".toRegex(RegexOption.IGNORE_CASE)
        private val INT_REGEX = "[-+]?(?:0|[1-9][0-9]*)".toRegex(RegexOption.IGNORE_CASE)

        private val ERROR_TRAILING_DATA = SimpleCommandExceptionType(translatable("argument.nbt.trailing").toMessage())
        private val ERROR_EXPECTED_KEY = SimpleCommandExceptionType(translatable("argument.nbt.expected.key").toMessage())
        private val ERROR_EXPECTED_VALUE = SimpleCommandExceptionType(translatable("argument.nbt.expected.value").toMessage())
        private val ERROR_INSERT_MIXED_LIST = Dynamic2CommandExceptionType { a, b ->
            translatable("argument.nbt.list.mixed", listOf(text(a.toString()), text(b.toString()))).toMessage()
        }
        private val ERROR_INSERT_MIXED_ARRAY = Dynamic2CommandExceptionType { a, b ->
            translatable("argument.nbt.array.mixed", listOf(text(a.toString()), text(b.toString()))).toMessage()
        }
        private val ERROR_INVALID_ARRAY = DynamicCommandExceptionType { translatable("argument.nbt.array.invalid", listOf(text(it.toString()))).toMessage() }
    }
}

private val Char.isQuotedStringStart: Boolean
    get() = StringReader.isQuotedStringStart(this)

private val StringReader.hasElementSeparator: Boolean
    get() {
        skipWhitespace()
        return if (canRead() && peek() == ',') {
            skip()
            skipWhitespace()
            true
        } else false
    }
