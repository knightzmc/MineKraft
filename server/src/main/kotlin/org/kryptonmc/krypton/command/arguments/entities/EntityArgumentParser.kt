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
package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import org.kryptonmc.krypton.KryptonServer

object EntityArgumentParser {

    fun parse(
        reader: StringReader,
        operation: Char,
        position: Int,
        onlyPlayers: Boolean,
        singleTarget: Boolean,
        server: KryptonServer
    ) = when (operation) {
        'p' -> {
            EntityQuery(listOf(), EntityQuery.SELECTOR.NEAREST_PLAYER)
        }
        'e' -> {
            if (singleTarget) {
                reader.cursor = 0
                throw TOO_MANY_ENTITIES.createWithContext(reader)
            } else if (onlyPlayers) {
                reader.cursor = 0
                throw ONLY_FOR_PLAYERS.createWithContext(reader)
            }
            EntityQuery(listOf(), EntityQuery.SELECTOR.ALL_ENTITIES)
        }
        'r' -> {
            EntityQuery(listOf(), EntityQuery.SELECTOR.RANDOM_PLAYER)
        }
        'a' -> {
            if (singleTarget) {
                reader.cursor = 0
                throw TOO_MANY_PLAYERS.createWithContext(reader)
            }
            if (reader.canRead() && reader.peek() == '[') {
                reader.skip()
                EntityQuery(parseArguments(reader, server), EntityQuery.SELECTOR.ALL_PLAYERS)
            } else {
                EntityQuery(listOf(), EntityQuery.SELECTOR.ALL_PLAYERS)
            }
        }
        's' -> {
            EntityQuery(listOf(), EntityQuery.SELECTOR.EXECUTOR)
        }
        else -> {
            reader.cursor = position
            throw UNKNOWN_SELECTOR_EXCEPTION.createWithContext(reader, "@$operation")
        }
    }

    private fun parseArguments(
        reader: StringReader,
        server: KryptonServer
    ): List<EntityArgument.EntityArg> {
        reader.skipWhitespace()
        val args = mutableListOf<EntityArgument.EntityArg>()
        while (reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace()
            val position = reader.cursor
            val option = reader.readString()
            if (option !in EntityArguments.ARGUMENTS) throw INVALID_OPTION.createWithContext(reader, option)
            reader.skipWhitespace()
            if (reader.canRead() && reader.peek() == '=') {
                reader.skip()
                reader.skipWhitespace()
                val value = reader.readString()
                if (value.isBlank()) throw VALUELESS_EXCEPTION.createWithContext(reader, option)

                args += EntityArgument.EntityArg(option, value, false)

                reader.skipWhitespace()
                if (!reader.canRead()) {
                    continue
                }

                if (reader.peek() == ',') {
                    reader.skip()
                    continue
                }

                if (reader.peek() != ']') {
                    throw UNTERMINATED_EXCEPTION.createWithContext(reader)
                }
                break
            }

            reader.cursor = position
            throw VALUELESS_EXCEPTION.createWithContext(reader, option)
        }
        if (reader.canRead()) {
            reader.skip()
        } else {
            throw UNTERMINATED_EXCEPTION.createWithContext(reader)
        }
        return args.toList()
    }


}
