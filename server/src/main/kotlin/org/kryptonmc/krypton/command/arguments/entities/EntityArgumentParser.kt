package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import org.kryptonmc.krypton.entity.player.KryptonPlayer

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
