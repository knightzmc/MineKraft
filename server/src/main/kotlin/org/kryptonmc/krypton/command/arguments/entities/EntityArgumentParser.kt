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

object EntityArgumentParser {

    fun parse(
        reader: StringReader,
        operation: Char,
    ) = when (operation) {
        'p' -> {
            EntityQuery(listOf(), EntityQuery.Operation.NEAREST_PLAYER)
        }
        'e' -> {
            EntityQuery(listOf(), EntityQuery.Operation.ALL_ENTITIES)
        }
        'r' -> {
            EntityQuery(listOf(), EntityQuery.Operation.RANDOM_PLAYER)
        }
        'a' -> {
            if(reader.canRead() && reader.peek() == '[') {
                EntityQuery(parseArguments(reader), EntityQuery.Operation.ALL_PLAYERS)
            } else {
                EntityQuery(listOf(), EntityQuery.Operation.ALL_PLAYERS)
            }
        }
        's' -> {
            EntityQuery(listOf(), EntityQuery.Operation.EXECUTOR)
        }
        else -> EntityQuery(listOf(), EntityQuery.Operation.UNKNOWN)
    }

    private fun parseArguments(
        reader: StringReader
    ): List<EntityArgument.EntityArg> {
        reader.skipWhitespace()
        val args = mutableListOf<EntityArgument.EntityArg>()
        while (reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace()
            reader.skip()
            var not = false
            val option = buildString {
                while (reader.canRead() && reader.peek() != '=') {
                    val c = reader.read()
                    append(c)
                    reader.skipWhitespace()
                }
            }
            reader.skipWhitespace()
            val value = buildString {
                if (reader.canRead() && reader.peek() == '=') {
                    reader.skip()
                    reader.skipWhitespace()
                    if (reader.peek() == '!') {
                        not = true
                        reader.skip()
                        reader.skipWhitespace()
                    }
                    while (reader.canRead() && reader.peek() != ',' && reader.peek() != ']') {
                        append(reader.read())
                        reader.skipWhitespace()
                    }
                }
                reader.skipWhitespace()
            }
            if (option in EntityArguments.ARGUMENTS) {
                args += EntityArgument.EntityArg(option, value, not)
            }
        }
        return args.toList()
    }


}
